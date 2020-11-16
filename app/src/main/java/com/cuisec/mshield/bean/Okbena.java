package com.cuisec.mshield.bean;

public class Okbena {

    /**
     * id : 3823
     * hitokoto : 免费？不存在的！别人总要从你那里拿走点什么，或者是名声或者是金钱。
     * type : e
     * from : 原创
     * from_who : null
     * creator : 墨影
     * creator_uid : 1963
     * reviewer : 0
     * uuid : 69ddaeb2-e330-407f-9548-dc25df746983
     * created_at : 1535191768
     */

    private int id;
    private String hitokoto;
    private String type;
    private String from;
    private Object from_who;
    private String creator;
    private int creator_uid;
    private int reviewer;
    private String uuid;
    private String created_at;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHitokoto() {
        return hitokoto;
    }

    public void setHitokoto(String hitokoto) {
        this.hitokoto = hitokoto;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Object getFrom_who() {
        return from_who;
    }

    public void setFrom_who(Object from_who) {
        this.from_who = from_who;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getCreator_uid() {
        return creator_uid;
    }

    public void setCreator_uid(int creator_uid) {
        this.creator_uid = creator_uid;
    }

    public int getReviewer() {
        return reviewer;
    }

    public void setReviewer(int reviewer) {
        this.reviewer = reviewer;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
