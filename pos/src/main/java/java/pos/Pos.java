package java.pos;

import java.pos.dao.ProductDao;
import java.pos.devices.output.Display;
import java.pos.devices.output.Printer;
import java.pos.listeners.BarcodeScanListener;
import java.pos.model.Product;
import java.pos.model.ProductsReceipt;
import java.pos.utils.MessageUtils;
import java.util.Optional;

/**
 * NIE OK
 * Bardzo dziwna nazwa (Pos)
 *
 * 1. Bardzo złożone - duża odpowiedzialność
 * 2. To powinien być serwis (komunikacja z bazą danych, główna logika, obliczenia, ..., "mózg aplikacji")
 * 3. Pomieszanie front-end(display, drukarka) i back-end(baza danych, DAO)
 *
 * To powinny być 2 odrębne elementy:
 * - listener (?) albo kontroler (front-end)
 * - serwis (back-end) (PosService i PosServiceImpl)
 *
 * Listener powinien być prosty
 */
public class Pos implements BarcodeScanListener {

    public static final String INVALID_BAR_CODE = "Invalid bar-code";
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String EXIT_CODE = "exit";

    private ProductDao productDao;
    private Display display;
    private Printer printer;
    private ProductsReceipt receipt = getNewProductReceipt();

    public Pos(ProductDao productDao, Display display, Printer printer) {

        this.productDao = productDao;
        this.display = display;
        this.printer = printer;
    }

    /**
     * OK
     * 1. walidacja - sprawdzanie danych
     * 2. decyzja co dalej
     * @param barcode
     */
    public void onBarcodeScan(String barcode) {
        if (isInvalidCode(barcode)) {
            handleInvalidCode();
        } else {
            handleValidCode(barcode);
        }
    }

    private void handleValidCode(String barcode) {
        if (isExitCode(barcode)) {
            handleExitCode();
        } else {
            Optional<Product> product = getProductByBarcode(barcode);
            handleScannedProduct(product);
        }
    }

    private Optional<Product> getProductByBarcode(String barcode) {
        return Optional.ofNullable(productDao.getByBarcode(barcode));
    }

    private void handleScannedProduct(Optional<Product> product) {
        if (product.isPresent()) {
            handleProductFound(product.get());
        } else {
            handleProductNotFound();
        }
    }

    private void handleProductNotFound() {
        display.showMessage(PRODUCT_NOT_FOUND);
    }

    private void handleProductFound(Product product) {
        String message = MessageUtils.getProductMessage(product);
        receipt.add(product);
        display.showMessage(message);
    }

    private void handleInvalidCode() {
        display.showMessage(INVALID_BAR_CODE);
    }

    private boolean isInvalidCode(String barcode) {
        return barcode == null || barcode.isEmpty();
    }

    private boolean isExitCode(String barcode) {
        return EXIT_CODE.equals(barcode);
    }

    public void handleExitCode() {
        /**
         * Java 8 style - iterowanie po elementach listy (lambda)
         */
        receipt.getAll().stream().forEach(product -> printer.printLine(MessageUtils.getProductMessage(product)));

        Double sum = receipt.getSum();
        printer.printLine(MessageUtils.getTotalSumMessage(sum));
        display.showMessage(MessageUtils.getTotalSumMessage(sum));
        receipt = getNewProductReceipt();
    }

    private ProductsReceipt getNewProductReceipt() {
        return new ProductsReceipt();
    }

}
