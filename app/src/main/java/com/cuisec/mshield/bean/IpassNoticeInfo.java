package com.cuisec.mshield.bean;

import java.util.List;

public class IpassNoticeInfo {


    /**
     * hasSuccess : true
     * code : 200
     * msg : 操作成功
     * data : {"pageIndex":1,"pageSize":10,"pageCount":1,"totalCount":6,"list":[{"id":6,"content":"您的iPASS订单有最新状态，单号null，状态为null。请至证书自助服务网站（http://ipass.uni-ca.com.cn:7007 ）查看订单详情。","readed":"1"},{"id":5,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"},{"id":4,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"},{"id":3,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"},{"id":2,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"},{"id":1,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"}]}
     */

    private boolean hasSuccess;
    private String code;
    private String msg;
    private DataBean data;

    public boolean isHasSuccess() {
        return hasSuccess;
    }

    public void setHasSuccess(boolean hasSuccess) {
        this.hasSuccess = hasSuccess;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * pageIndex : 1
         * pageSize : 10
         * pageCount : 1
         * totalCount : 6
         * list : [{"id":6,"content":"您的iPASS订单有最新状态，单号null，状态为null。请至证书自助服务网站（http://ipass.uni-ca.com.cn:7007 ）查看订单详情。","readed":"1"},{"id":5,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"},{"id":4,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"},{"id":3,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"},{"id":2,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"},{"id":1,"content":"您的数字证书(证书序列号:CUS32100148263)即将于null到期，如需继续使用， 请访问iPASS用户自服务网站（http://ipass.uni-ca.com.cn:7007 ）办理iPASS续费业务，及时更新您的证书,已更新请忽略","readed":"1"}]
         */

        private int pageIndex;
        private int pageSize;
        private int pageCount;
        private int totalCount;
        private List<ListBean> list;

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * id : 6
             * content : 您的iPASS订单有最新状态，单号null，状态为null。请至证书自助服务网站（http://ipass.uni-ca.com.cn:7007 ）查看订单详情。
             * readed : 1
             */

            private int id;
            private String content;
            private String readed;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getReaded() {
                return readed;
            }

            public void setReaded(String readed) {
                this.readed = readed;
            }
        }
    }
}
