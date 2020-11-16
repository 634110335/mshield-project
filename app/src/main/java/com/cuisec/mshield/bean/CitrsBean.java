package com.cuisec.mshield.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class CitrsBean  implements Parcelable {


    /**
     * conut : 10
     * domain_list : [{"detailUrl":"https://www.chinabidding.cn//zbgs/Un1YF.html","id":"151205","releasetime":"2020-04-29 15:43:56","source":"联通采购招标网","title":"中国联通电子商城北京市分公司增值业务类、OSS类、信息化类应用软件开发产品公开招募项目第二次补充招募结果公示"},{"detailUrl":"https://www.chinabidding.cn//zbgg/Un1EQ.html","id":"151199","releasetime":"2020-04-29 15:43:56","source":"联通采购招标网","title":"2019年中国联通物联网专用网元扩容一期工程_北京市分公司单项工程-智能解析DNS采购（第二次）招标公告"},{"detailUrl":"https://www.chinabidding.cn//zbgs/Un1vX.html","id":"151192","releasetime":"2020-04-29 15:43:45","source":"联通采购招标网","title":"中国联通电子商城北京市分公司公开市场2019-2021年中国联通北京社会宏站资源产品公开招募项目增补招募结果公示"},{"detailUrl":"http://www.ai8.com.cn/n-zb-6809645.html","id":"146754","releasetime":"2020-04-28 22:21:43","source":"联通采购招标网","title":"北京市糖尿病研究所糖尿病视网膜病变生物标志物与个体化诊疗研究临床检验设备招标公告"},{"detailUrl":"http://www.ai8.com.cn/n-zb-6811850.html","id":"146604","releasetime":"2020-04-28 22:12:43","source":"联通采购招标网","title":"北京市通州区司法局北京城市副中心(通州区)\u201c十四五\u201d法治政府建设规划前期重大课题研究服务招标公告"},{"detailUrl":"http://www.ai8.com.cn/n-zb-6814652.html","id":"146489","releasetime":"2020-04-28 22:05:02","source":"联通采购招标网","title":"北京市民政局社会福利领域资金监管采购项目竞争性磋商公告"},{"detailUrl":"http://www.ai8.com.cn/n-zb-6814696.html","id":"146487","releasetime":"2020-04-28 22:05:02","source":"联通采购招标网","title":"[石景山]北京市石景山区经济和信息化局领导驾驶舱采购项目招标公告"},{"detailUrl":"http://www.ai8.com.cn/n-zb-6802586.html","id":"144086","releasetime":"2020-04-28 19:27:48","source":"联通采购招标网","title":"北京市大中型水库移民后期扶持\u201c十四五\u201d规划编制招标公告"},{"detailUrl":"http://www.ai8.com.cn/n-zb-6802630.html","id":"144081","releasetime":"2020-04-28 19:27:26","source":"联通采购招标网","title":"北京市海淀区体育场馆管理中心申报\u201c海淀体育中心物业管理服务\u201d政府采购公告"},{"detailUrl":"http://www.ai8.com.cn/n-zb-6808997.html","id":"143535","releasetime":"2020-04-28 19:13:20","source":"联通采购招标网","title":"北京市房山区第一医院所属房山区第一医院神经-心理-睡眠专业建设项目竞争性磋商公告"}]
     * resultcode : 0
     * resultdesc : 操作成功
     */

    private int conut;
    private String resultcode;
    private String resultdesc;
    private List<DomainListBean> domain_list;

    protected CitrsBean(Parcel in) {
        conut = in.readInt();
        resultcode = in.readString();
        resultdesc = in.readString();
        domain_list = in.createTypedArrayList(DomainListBean.CREATOR);
    }

    public static final Creator<CitrsBean> CREATOR = new Creator<CitrsBean>() {
        @Override
        public CitrsBean createFromParcel(Parcel in) {
            return new CitrsBean(in);
        }

        @Override
        public CitrsBean[] newArray(int size) {
            return new CitrsBean[size];
        }
    };

    public int getConut() {
        return conut;
    }

    public void setConut(int conut) {
        this.conut = conut;
    }

    public String getResultcode() {
        return resultcode;
    }

    public void setResultcode(String resultcode) {
        this.resultcode = resultcode;
    }

    public String getResultdesc() {
        return resultdesc;
    }

    public void setResultdesc(String resultdesc) {
        this.resultdesc = resultdesc;
    }

    public List<DomainListBean> getDomain_list() {
        return domain_list;
    }

    public void setDomain_list(List<DomainListBean> domain_list) {
        this.domain_list = domain_list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(conut);
        dest.writeString(resultcode);
        dest.writeString(resultdesc);
        dest.writeTypedList(domain_list);
    }

    public static class DomainListBean implements Parcelable{
        /**
         * detailUrl : https://www.chinabidding.cn//zbgs/Un1YF.html
         * id : 151205
         * releasetime : 2020-04-29 15:43:56
         * source : 联通采购招标网
         * title : 中国联通电子商城北京市分公司增值业务类、OSS类、信息化类应用软件开发产品公开招募项目第二次补充招募结果公示
         */

        private String detailUrl;
        private String id;
        private String releasetime;
        private String source;
        private String title;
        private String bidType;
        protected DomainListBean(Parcel in) {
            detailUrl = in.readString();
            id = in.readString();
            releasetime = in.readString();
            source = in.readString();
            title = in.readString();
            bidType = in.readString();
        }

        public String getBidType() {
            return bidType;
        }

        public void setBidType(String bidType) {
            this.bidType = bidType;
        }

        public static final Creator<DomainListBean> CREATOR = new Creator<DomainListBean>() {
            @Override
            public DomainListBean createFromParcel(Parcel in) {
                return new DomainListBean(in);
            }

            @Override
            public DomainListBean[] newArray(int size) {
                return new DomainListBean[size];
            }
        };

        public String getDetailUrl() {
            return detailUrl;
        }

        public void setDetailUrl(String detailUrl) {
            this.detailUrl = detailUrl;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getReleasetime() {
            return releasetime;
        }

        public void setReleasetime(String releasetime) {
            this.releasetime = releasetime;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(detailUrl);
            dest.writeString(id);
            dest.writeString(releasetime);
            dest.writeString(source);
            dest.writeString(title);
            dest.writeString(bidType);
        }
    }
}