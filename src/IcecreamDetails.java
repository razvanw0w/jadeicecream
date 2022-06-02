public class IcecreamDetails {
    private Integer quantity;
    private Integer price;

    public IcecreamDetails(Integer quantity, Integer price) {
        this.quantity = quantity;
        this.price = price;
    }

    public void decreaseQuantity(Integer purchasedQuantity) {
        quantity -= purchasedQuantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
