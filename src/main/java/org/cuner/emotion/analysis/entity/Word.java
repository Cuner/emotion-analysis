package org.cuner.emotion.analysis.entity;

/**
 * Created by houan on 18/3/14.
 */
public class Word implements Cloneable {

    public Integer dicId;

    public String name;

    public Integer weight;

    public Integer category; //词的类目 具体类目请看 WordConstant

    public Integer relevance; // 若word 为近义词, 则值为词根的主键wordId

    public String extra;

    public Word() {
    }

    public Word(Integer dicId, String name, Integer weight, Integer category, Integer relevance, String extra) {
        this.dicId = dicId;
        this.name = name;
        this.weight = weight;
        this.category = category;
        this.relevance = relevance;
        this.extra = extra;
    }

    public Integer getDicId() {
        return dicId;
    }

    public void setDicId(Integer dicId) {
        this.dicId = dicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getRelevance() {
        return relevance;
    }

    public void setRelevance(Integer relevance) {
        this.relevance = relevance;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public Object clone() {
        Word ret = null;
        try {
            ret = (Word) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if(getClass() != obj.getClass())
            return false;

        Word other = (Word) obj;
        if (dicId == null) {
            if (other.getDicId() != null) {
                return false;
            }
        } else if (!dicId.equals(other.getDicId())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return dicId;
    }

    @Override
    public String toString() {
        return "Word{" +
                "dicId=" + dicId +
                ", name='" + name + '\'' +
                ", weight=" + weight +
                ", category=" + category +
                ", relevance=" + relevance +
                ", extra='" + extra + '\'' +
                '}';
    }
}
