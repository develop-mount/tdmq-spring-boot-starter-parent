package com.eqxiu.tdmq.example;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 场景信息
 *
 * @author linfeng-eqxiu
 * @date 2019/8/2
 */
public class SceneInfoDto implements Serializable {

    private static final long serialVersionUID = -8405370798984429935L;

    /**
     * 审核唯一ID
     */
    private String checkId;
    /**
     * 应用ID，企业ID
     */
    private Long appId;
    /**
     * 产品ID
     */
    private Long productId;
    /**
     * 举报标签id
     */
    private Long reportTagId;

    public Long getReportTagId() {
        return reportTagId;
    }

    public void setReportTagId(Long reportTagId) {
        this.reportTagId = reportTagId;
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    /**
     * 举报原因
     */
    private String reportMessage;

    /**
     * 额外信息
     */
    private String extra;
    /**
     * 应用Key
     */
    private String appKey;
    /**
     * 产品类型
     */
    private String productKey;
    /**
     * 场景ID，第三方信息
     */
    private String sceneId;
    /**
     * 场景CODE
     */
    private String sceneCode;
    /**
     * 标题
     */
    private String title;
    /**
     * 封面地址
     */
    private String cover;
    /**
     * 描述
     */
    private String description;
    /**
     * 预览地址
     */
    private String viewUrl;
    /**
     * 创建用户,用户登录名
     */
    private String createUser;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 发布时间
     */
    private Date publishTime;
    /**
     * 是否第二次
     */
    private Integer recheck;
    /**
     * 来源
     */
    private Integer platform;
    /**
     * 发布人类型
     */
    private String userMember;
    /**
     * 业务类型
     */
    private Integer businessType;
    /**
     * 菜单类型
     */
    private Integer menuType;
    /**
     * 队列类型
     */
    private Integer queueType;
    /**
     * 元素个数
     */
    private Integer elementCount;
    /**
     * 场景元素信息
     */
    private List<ElementInfoDto> elementInfoList;

    public SceneInfoDto() {
    }

    public String getCheckId() {
        return checkId;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Integer getRecheck() {
        return recheck;
    }

    public void setRecheck(Integer recheck) {
        this.recheck = recheck;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public String getUserMember() {
        return userMember;
    }

    public void setUserMember(String userMember) {
        this.userMember = userMember;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public Integer getMenuType() {
        return menuType;
    }

    public void setMenuType(Integer menuType) {
        this.menuType = menuType;
    }

    public Integer getQueueType() {
        return queueType;
    }

    public void setQueueType(Integer queueType) {
        this.queueType = queueType;
    }

    public Integer getElementCount() {
        return elementCount;
    }

    public void setElementCount(Integer elementCount) {
        this.elementCount = elementCount;
    }

    public List<ElementInfoDto> getElementInfoList() {
        return elementInfoList;
    }

    public void setElementInfoList(List<ElementInfoDto> elementInfoList) {
        this.elementInfoList = elementInfoList;
    }


    @Override
    public String toString() {
        return "SceneInfoDto{" +
                "checkId='" + checkId + '\'' +
                ", appId=" + appId +
                ", productId=" + productId +
                ", reportTagId=" + reportTagId +
                ", reportMessage='" + reportMessage + '\'' +
                ", extra='" + extra + '\'' +
                ", appKey='" + appKey + '\'' +
                ", productKey='" + productKey + '\'' +
                ", sceneId='" + sceneId + '\'' +
                ", sceneCode='" + sceneCode + '\'' +
                ", title='" + title + '\'' +
                ", cover='" + cover + '\'' +
                ", description='" + description + '\'' +
                ", viewUrl='" + viewUrl + '\'' +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", publishTime=" + publishTime +
                ", recheck=" + recheck +
                ", platform=" + platform +
                ", userMember='" + userMember + '\'' +
                ", businessType=" + businessType +
                ", menuType=" + menuType +
                ", queueType=" + queueType +
                ", elementCount=" + elementCount +
                ", elementInfoList=" + elementInfoList +
                '}';
    }
}
