# Patient Monitor

## Urgent Rescue

- listen for socket.io messages, redirect to frontend
- gather all messages to detect exact position
- when responded, cancel alert for ap
- save current position to tracks


## Patient Tracing

- listen for socket.io messages, cache the tracks for given periods ( 5s )
- positioning and save to tracks

> 
  - input> ap: bracelet:rssi:time
  - cache> bracelet stack of size 5 (when full or expired, pop all): ap-lnglat:distance
  - db> bracelet: position


## API

- respond to emergency [PUT /rescue]

  - request body
  
      {
        "apid": "83",
        "response": "doctorid"
      }

  - response body
  
      {
        "status": "ok"
      }

- bracelet tracks [GET /track/{bracelet}]

  - response body

      {
        "status": "ok",
        "data": {
          "list": [{
            "floor": 2,
            "timestamp": 1478159590006,
            "gps": {"lng" : 104.061346, "lat" : 30.641574}
          }]
      }

- bracelet last position [GET /track/{bracelet}/last]

  - response body

      {
        "status": "ok",
        "data": {
          "address": "West 204",
          "floor": 2,
          "timestamp": 1478159590006,
          "gps": {"lng" : 104.061346, "lat" : 30.641574}
        }
      }

- bind bracelet to patient [PUT /bracelet]

  - request body (性别: 0-女, 1-男)

      {
        "bracelet": "83",
        "patientName": "Wang Nima",
        "patientGender": "1",
        "patientRemark": "remark",
        "patientPhone": "13800138000"
      }

  - response body

      {
        "status": "ok"
      }

- unbind bracelet [PUT /bracelet/binded/{bracelet}]

  - request body (性别: 0-女, 1-男)

      {
        "bracelet": "581b1a6542aa101eebc77e60",
        "name": "82"
      }

  - response body

      {
        "status": "ok"
      }

- bracelet list [GET /bracelet{?binded}] (binded: 0,1; true,false)

  - response body (not binded)

     {
       "status": "ok",
       "data": {
        "list": [{
          "bracelet": "581b1a6542aa101eebc77e60",
          "name": "82"
        }]
       }
     }


  - response body (binded)

     {
       "status": "ok",
       "data": {
        "list": [{
          "bracelet": "581b1a6542aa101eebc77e60",
          "name": "82",
          "patientName": "Wang Nima",
          "patientGender": "1",
          "patientRemark": "remark",
          "patientPhone": "13800138000"
        }]
       }
     }


## Yunba.io
      
- broadcast msg for AP detected emergency:

      {
        "topic": "demo",
        "message": {"apid": "ap110", "floor": 2, "address": "West 208", "alert": "y", "payload": "126683000000"}
      }

- emergency response alias push:

      {
        "alias": "ap110",
        "message": "Medic on the way"
      }


## Data Model

- AP

column | type | description
-------|------|------------
id | string | AP id
gps | LngLat | AP location
status | string | 
create | Date | create date


- Bracelet Trace

column | type | description
-------|------|------------
id | ObjectId | mongo default `_id`
bracelet | string | bracelet id
ap | string | ap id
rssi | number | detected RSSI
create | Date | create date of the trace


- Bracelet Position

column | type | description
-------|------|------------
id | string | bracelet id
position | array | LngLat with timestamp, position tracks of the bracelet
