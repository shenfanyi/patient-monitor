package com.fintech.hospital.domain;

import com.alibaba.fastjson.annotation.JSONField;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.*;

/**
 * @author baoqiang
 */
public class Bracelet {

  @JSONField(name = "bracelet", serialize = false)
  public void setBracelet(String bracelet) {
    this.id = new ObjectId(bracelet);
  }

  @JSONField(name = "bracelet", deserialize = false)
  @Id
  private ObjectId id;

  @Transient
  @Field("macAddress")
  private String mac;

  @Field("deviceName")
  private String name;

  @JSONField(serialize = false)
  private Integer status;

  @Transient
  private String type;

  @Transient
  @Field("create_on")
  private String create;

  public void bindPatient(Bracelet bracelet) {
    setPatientName(bracelet.patientName);
    setPatientDBGender(bracelet.patientDBGender);
    setPatientRemark(bracelet.patientRemark);
    setPatientPhone(bracelet.patientPhone);
    setStatus(1);
  }

  public void unbindPatient(){
    setPatientName(null);
    setPatientDBGender(null);
    setPatientRemark(null);
    setPatientPhone(null);
    setStatus(0);
  }

  @JSONField(serialize = false)
  private String patientDBGender;

  public String getPatientDBGender() {
    return patientDBGender;
  }

  public void setPatientDBGender(String patientDBGenger) {
    if("M".equals(patientDBGenger)) this.patientGender = "1";
    else if("F".equals(patientDBGenger)) this.patientGender = "0";
    this.patientDBGender = patientDBGenger;
  }

  /**
   * following fields are used in request body
   */
  @NotNull
  @Size(min = 1, max = 22)
  private String patientName;

  @Transient
  @NotNull
  @DecimalMax("1")
  @DecimalMin("0")
  @Digits(integer = 1, fraction = 0)
  private String patientGender;

  @Size(max = 128)
  private String patientRemark;

  @NotNull
  @Size(min = 11, max = 11)
  @Digits(integer = 11, fraction = 0)
  private String patientPhone;

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getPatientName() {
    return patientName;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  public String getPatientGender() {
    return patientGender;
  }

  public void setPatientGender(String patientGender) {
    if("1".equals(patientGender)) this.patientDBGender = "M";
    else if("0".equals(patientGender)) this.patientDBGender = "F";
    this.patientGender = patientGender;
  }

  public String getPatientRemark() {
    return patientRemark;
  }

  public void setPatientRemark(String patientRemark) {
    this.patientRemark = patientRemark;
  }

  public String getPatientPhone() {
    return patientPhone;
  }

  public void setPatientPhone(String patientPhone) {
    this.patientPhone = patientPhone;
  }

  public ObjectId getId() {
    return id;
  }

  @JSONField(deserialize = false)
  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getMac() {
    return mac;
  }

  public void setMac(String mac) {
    this.mac = mac;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCreate() {
    return create;
  }

  public void setCreate(String create) {
    this.create = create;
  }
}