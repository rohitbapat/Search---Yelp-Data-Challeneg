package task1;

import java.util.List;


public class BusinessReviewTips {

	private String business_id;
	private List<String> categories;
	private String reviews;
	private String tips;
	public String getBusiness_id() {
		return business_id;
	}
	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public String getReviews() {
		return reviews;
	}
	public void setReviews(String reviews) {
		this.reviews = reviews;
	}
	public String getTips() {
		return tips;
	}
	public void setTips(String tips) {
		this.tips = tips;
	}
	@Override
	public String toString() {
		return "BusinessReviewTips [business_id=" + business_id + ", categories=" + categories + ", reviews=" + reviews
				+ ", tips=" + tips + "]";
	}
	
	
	
	
	
}
