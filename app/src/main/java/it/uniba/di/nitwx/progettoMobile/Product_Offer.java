package it.uniba.di.nitwx.progettoMobile;



import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import it.uniba.di.nitwx.progettoMobile.dummy.OfferContent;
import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;


@Entity(foreignKeys = { @ForeignKey(entity = ProductContent.Product.class,
                                            parentColumns = "id",
                                            childColumns = "product_id"),
                        @ForeignKey(entity = OfferContent.Offer.class,
                                            parentColumns = "id",
                                            childColumns = "offer_id")})
public class Product_Offer {
    public int quantity;
    public int offer_id;
    public int product_id;

    public Product_Offer(int quantity, int offer_id, int product_id ) {
        this.quantity = quantity;
        this.offer_id = offer_id;
        this.product_id = product_id;
    }


}