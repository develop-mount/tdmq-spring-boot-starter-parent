package com.eqxiu.tdmq.example;

import java.io.Serializable;
import java.util.Objects;

/**
 * 场景元素信息
 *
 * @author linfeng-eqxiu
 * @date 2019/8/2
 */
public class ElementInfoDto implements Serializable {

    private static final long serialVersionUID = -3162596463723938865L;

    /**
     * 元素ID，审核自己生成的
     */
    private String elementId;
    /**
     * 素材ID，业务侧传递的
     */
    private String materialId;
    /**
     * 审核唯一id
     */
    private String checkId;
    /**
     * 内容类型
     */
    private Integer type;
    /**
     * 具体内容
     */
    private String content;

    /**
     * 应用ID
     */
    private Long appId;

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getCheckId() {
        return checkId;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ElementInfoDto() {
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ElementInfoDto)) {
            return false;
        }
        ElementInfoDto that = (ElementInfoDto) o;
        return Objects.equals(getType(), that.getType()) &&
                Objects.equals(getContent(), that.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getContent());
    }


}
