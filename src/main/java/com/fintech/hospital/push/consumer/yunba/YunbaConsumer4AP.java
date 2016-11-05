package com.fintech.hospital.push.consumer.yunba;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.data.Cache;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.APMsg;
import com.fintech.hospital.domain.Bracelet;
import com.fintech.hospital.domain.BraceletTrace;
import com.fintech.hospital.domain.TimedPosition;
import com.fintech.hospital.push.PushService;
import com.fintech.hospital.push.model.PushMsg;
import com.fintech.hospital.push.supplier.yunba.YunbaOpts;
import com.fintech.hospital.rssi.RssiDistanceModel;
import com.fintech.hospital.rssi.RssiMeasure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.fintech.hospital.domain.TimedPosition.mean;
import static com.fintech.hospital.push.model.PushType.BROADCAST;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Service("yunbaConsumer4AP")
@Scope(SCOPE_SINGLETON)
public class YunbaConsumer4AP extends YunbaConsumer {


  YunbaConsumer4AP(@Value("${yunba.server.url}") String yunbaServerUrl,
                   @Value("${yunba.appkey.ap}") String yunbaAppKey) {
    super(yunbaServerUrl, yunbaAppKey);
  }

  @Autowired
  private PushService pushService;

  @Autowired
  private MongoDB mongo;

  @Autowired
  private Cache cache;

//  final RssiDistanceModel RSSI_MODEL = new RssiDistanceModel(-0.8204588, 2.1541073, 5.6846953, -60);
  final RssiDistanceModel RSSI_MODEL = new RssiDistanceModel(0.1820634, 0.8229884, 6.6525179, -75);

  @Value("${yunba.rescue.topic}")
  private String RESCUE_TOPIC;

  @Value("${yunba.trace.alias}")
  private String TRACE_ALIAS;

  @Value("${distance.coords.euclidean}")
  private boolean USE_EUCLIDEAN;

  @Override
  public void consume(String msg) {
    LOG.info("consuming ap msg... {}", msg);
    APMsg apMsg = JSON.parseObject(msg, APMsg.class);

    final long current = System.currentTimeMillis();
    final Bracelet bracelet = mongo.getBracelet(apMsg.braceletBleId());
    final String braceletId = bracelet.getId().toHexString();

    /* query ap */
    supplyAsync(() -> mongo.getAP(apMsg.getApid())
    ).thenCompose(ap -> {
      if (ap == null) throw new IllegalArgumentException("ap not exists " + apMsg.getApid());

      /* categorize msg type: urgency (push to mon immediately for alert), tracing */
      if (apMsg.urgent()) {
        LOG.info("bracelet {}(BLE-ID) in emergency, detected by ap {}", apMsg.braceletBleId(), apMsg.getApid());
        apMsg.fillAP(ap);
        List<TimedPosition> positionList = mongo.getBraceletTrack(braceletId).getPosition();
        apMsg.setPosition(positionList.get(positionList.size() - 1));
        apMsg.setBracelet(braceletId);
        String alertMsg = String.format("%s (%s) 求救 ", bracelet.getPatientName(), apMsg.braceletBleId());
        apMsg.setMessage(alertMsg);
        String broadcast = JSON.toJSONString(apMsg);
        pushService.push2Mon(new PushMsg(BROADCAST, RESCUE_TOPIC, broadcast, new YunbaOpts(new YunbaOpts.YunbaAps(
            broadcast, alertMsg
        ))));
      }

      runAsync(() -> mongo.addBraceletTrace(
          braceletId,
          new BraceletTrace(apMsg.getApid(), apMsg.getRssi(), ap.getGps())
      ));
      /* pop all latest positions */
      return supplyAsync(() ->
          cache.push(braceletId, ap.getAlias(), new TimedPosition(ap, current, RSSI_MODEL.distance(apMsg.getRssi())))
      );
    }).thenAccept(positions -> {
      /* cache bandid, lnglatDistance and ap lnglat to list */
      if (positions == null || positions.isEmpty()) return;
      LOG.info("positioning bracelet {}", bracelet);
      TimedPosition braceletPosition = null;
      switch (positions.size()) {
        case 1:
          braceletPosition = positions.get(0);
          break;
        case 2:
          TimedPosition pos0 = positions.get(0),
              pos1 = positions.get(1);
          double distRatio0 = pos0.getRadius() / (pos0.getRadius() + pos1.getRadius());
          braceletPosition = mean(positions, new double[]{1 - distRatio0, distRatio0});
          break;
        default:
          braceletPosition = RssiMeasure.positioning(positions, braceletId, USE_EUCLIDEAN);
          break;
      }
      mongo.addBraceletPosition(braceletId, braceletPosition);
      LOG.info("new position {} for bracelet {} ", braceletPosition, bracelet);
    }).exceptionally(t -> {
      LOG.error("bracelet " + bracelet + " err...: ", t);
      return null;
    });

  }

  @Override
  public void onConnAck(Object json) throws Exception {
    LOG.info("yunba for ap {} connected {}", current, json);
    alias(TRACE_ALIAS);
  }

}
