package com.fintech.hospital.data;

import com.fintech.hospital.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author baoqiang
 */
@Component
public class MongoDB {

  final Logger LOG = LoggerFactory.getLogger(MongoDB.class);

  @Autowired
  private MongoTemplate template;

  @Value("${db.collection.ap}")
  private String DB_AP;

  @Value("${db.collection.bracelet.position}")
  private String DB_BP;

  @Value("${db.collection.bracelet.trace}")
  private String DB_BT;

  @Value("${db.collection.bracelet}")
  private String DB_BRACELET;

  public AP getAP(String apid) {
    return template.findOne(new Query(where("id").is(apid)), AP.class, DB_AP);
  }

  public void addBraceletTrace(String bracelet, BraceletTrace trace) {
    /* add new trace to bracelet positions */
    trace.setBracelet(bracelet);
    template.insert(trace, DB_BT);
    LOG.info("{} new trace added for {}", trace.getId(), bracelet);
  }

  public void addBraceletPosition(String bracelet, TimedPosition pos) {
    template.updateFirst(
        new Query(where("_id").is(bracelet)),
        new Update().push("position", pos),
        BraceletPosition.class,
        DB_BP
    );
  }

  public BraceletPosition getBraceletTrack(String bracelet) {
    return template.findOne(
        new Query(where("_id").is(bracelet)),
        BraceletPosition.class,
        DB_BP
    );
  }

  public List<Bracelet> braceletList(boolean binded) {
    return template.find(
        new Query(where("status").is(binded?1:0)),
        Bracelet.class,
        DB_BRACELET
    );
  }

  public Bracelet getBracelet(String idInBLE){
    return template.findOne(
        new Query(where("name").is(idInBLE)),
        Bracelet.class,
        DB_BRACELET
    );
  }

  public Bracelet bindBracelet(Bracelet fromPatient){
    Bracelet let = template.findOne(
        new Query(where("_id").is(fromPatient.getId()).and("status").is(0)),
        Bracelet.class,
        DB_BRACELET
    );
    if(let==null) return null;
    let.bindPatient(fromPatient);
    template.save(let, DB_BRACELET);
    return let;
  }

  public Bracelet unbindBracelet(String bracelet){
    Bracelet let = template.findOne(
        new Query(where("_id").is(bracelet).and("status").is(1)),
        Bracelet.class,
        DB_BRACELET
    );
    if(let ==null) return null;
    let.unbindPatient();
    template.save(let, DB_BRACELET);
    return let;
  }

}