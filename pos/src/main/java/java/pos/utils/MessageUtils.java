package java.pos.utils;

import java.pos.model.Product;

public class MessageUtils {

    public static String getProductMessage(Product product){
        return new StringBuilder()
                .append(product.getName())
                .append(" ")
                .append(product.getPrice())
                .toString();
    }

    public static String getTotalSumMessage(Double sum){
        return sum.toString();
    }
}
