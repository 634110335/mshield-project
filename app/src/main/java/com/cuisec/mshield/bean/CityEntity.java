package com.cuisec.mshield.bean;

import java.util.List;

public class CityEntity{

    /**
     * code : 200
     * message : 成功!
     * result : [{"sid":"30937089","text":"天气真好出去走走","type":"video","thumbnail":"http://wimg.spriteapp.cn/picture/2020/0405/5e8963ea41b7c__b.jpg","video":"http://uvideo.spriteapp.cn/video/2020/0405/5e8963ea8e8fb_wpd.mp4","images":null,"up":"91","down":"10","forward":"0","comment":"5","uid":"23180087","name":"亚瑟小娇妻佩奇","header":"http://wimg.spriteapp.cn/profile/large/2019/11/06/5dc29b8a6e4c6_mini.jpg","top_comments_content":"这也就是闺女，如果是儿子的话肯定就直接让拉车了","top_comments_voiceuri":"","top_comments_uid":"7442235","top_comments_name":"门头包","top_comments_header":"http://tp1.sinaimg.cn/3177288052/50/5657880483/1","passtime":"2020-04-10 02:40:01"},{"sid":"30965644","text":"大叔万一刹车怎么办呢？","type":"video","thumbnail":"http://wimg.spriteapp.cn/picture/2020/0407/5e8b67ecbb3bd_wpd.jpg","video":"http://uvideo.spriteapp.cn/video/2020/0407/5e8b67ecbb3bd_wpd.mp4","images":null,"up":"112","down":"7","forward":"0","comment":"13","uid":"23198267","name":"小百百互关","header":"http://thirdwx.qlogo.cn/mmopen/vi_32/qzR8tVGibFoOtyJLicrIjGrFwTU68SfHBibA72icFJQuAVicEUHET7o15ZTbHDvrqujicxbDAqeTvcSxadOqVlBcnyOg/132","top_comments_content":"有钱人无所不能","top_comments_voiceuri":"","top_comments_uid":"20723005","top_comments_name":"白河蒹葭Hc3","top_comments_header":"http://wimg.spriteapp.cn/profile/large/2019/01/08/5c34940d81d01_mini.jpg","passtime":"2020-04-07 19:27:20"}]
     */

    private int code;
    private String message;
    private List<ResultBean> result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * sid : 30937089
         * text : 天气真好出去走走
         * type : video
         * thumbnail : http://wimg.spriteapp.cn/picture/2020/0405/5e8963ea41b7c__b.jpg
         * video : http://uvideo.spriteapp.cn/video/2020/0405/5e8963ea8e8fb_wpd.mp4
         * images : null
         * up : 91
         * down : 10
         * forward : 0
         * comment : 5
         * uid : 23180087
         * name : 亚瑟小娇妻佩奇
         * header : http://wimg.spriteapp.cn/profile/large/2019/11/06/5dc29b8a6e4c6_mini.jpg
         * top_comments_content : 这也就是闺女，如果是儿子的话肯定就直接让拉车了
         * top_comments_voiceuri :
         * top_comments_uid : 7442235
         * top_comments_name : 门头包
         * top_comments_header : http://tp1.sinaimg.cn/3177288052/50/5657880483/1
         * passtime : 2020-04-10 02:40:01
         */

        private String sid;
        private String text;
        private String type;
        private String thumbnail;
        private String video;
        private Object images;
        private String up;
        private String down;
        private String forward;
        private String comment;
        private String uid;
        private String name;
        private String header;
        private String top_comments_content;
        private String top_comments_voiceuri;
        private String top_comments_uid;
        private String top_comments_name;
        private String top_comments_header;
        private String passtime;

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getVideo() {
            return video;
        }

        public void setVideo(String video) {
            this.video = video;
        }

        public Object getImages() {
            return images;
        }

        public void setImages(Object images) {
            this.images = images;
        }

        public String getUp() {
            return up;
        }

        public void setUp(String up) {
            this.up = up;
        }

        public String getDown() {
            return down;
        }

        public void setDown(String down) {
            this.down = down;
        }

        public String getForward() {
            return forward;
        }

        public void setForward(String forward) {
            this.forward = forward;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getTop_comments_content() {
            return top_comments_content;
        }

        public void setTop_comments_content(String top_comments_content) {
            this.top_comments_content = top_comments_content;
        }

        public String getTop_comments_voiceuri() {
            return top_comments_voiceuri;
        }

        public void setTop_comments_voiceuri(String top_comments_voiceuri) {
            this.top_comments_voiceuri = top_comments_voiceuri;
        }

        public String getTop_comments_uid() {
            return top_comments_uid;
        }

        public void setTop_comments_uid(String top_comments_uid) {
            this.top_comments_uid = top_comments_uid;
        }

        public String getTop_comments_name() {
            return top_comments_name;
        }

        public void setTop_comments_name(String top_comments_name) {
            this.top_comments_name = top_comments_name;
        }

        public String getTop_comments_header() {
            return top_comments_header;
        }

        public void setTop_comments_header(String top_comments_header) {
            this.top_comments_header = top_comments_header;
        }

        public String getPasstime() {
            return passtime;
        }

        public void setPasstime(String passtime) {
            this.passtime = passtime;
        }
    }
}