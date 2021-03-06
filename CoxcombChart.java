import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@DefaultProperty("children")
public class CoxcombChart extends Region {
    
    private static final double                PREFERRED_WIDTH  = 450;
    private static final double                PREFERRED_HEIGHT = 430;
    private static final double                MINIMUM_WIDTH    = 200;
    private static final double                MINIMUM_HEIGHT   = 200;
    private static final double                MAXIMUM_WIDTH    = 1024;
    private static final double                MAXIMUM_HEIGHT   = 1024;
    private              double                size;
    private              double                width;
    private              double                height;
    private              Canvas                canvas;
    private              GraphicsContext       ctx;
    private              Pane                  pane;
    private              LinkedList<Item>      items;
    private              Color                 _textColor;
    private              ObjectProperty<Color> textColor;
    private              boolean               _autoTextColor;
    private              BooleanProperty       autoTextColor;


    // ******************** Constructors **************************************
    public CoxcombChart() {
        this(new ArrayList<>());
    }
    public CoxcombChart(final Item... ITEMS) {
        this(Arrays.asList(ITEMS));
    }
    public CoxcombChart(final List<Item> ITEMS) {
        //getStylesheets().add(CoxcombChart.class.getResource("coxcomb-chart.css").toExternalForm());
        width          = PREFERRED_WIDTH;
        height         = PREFERRED_HEIGHT;
        size           = PREFERRED_WIDTH;
        items          = new LinkedList<>(ITEMS);
        _textColor     = Color.WHITE;
        _autoTextColor = true;
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        
        setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);

        getStyleClass().add("coxcomb-chart");

        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx    = canvas.getGraphicsContext2D();

        ctx.setLineCap(StrokeLineCap.BUTT);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.setTextAlign(TextAlignment.CENTER);

        pane = new Pane(canvas);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    // ******************** Methods *******************************************
    @Override public void layoutChildren() {
        super.layoutChildren();
    }

    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public List<Item> getItems() { return items; }
    public void setItems(final Item... ITEMS) {
        setItems(Arrays.asList(ITEMS));
    }
    public void setItems(final List<Item> ITEMS) {
        items.clear();
        ITEMS.forEach(item -> items.add(item));
        redraw();
    }
    public void addItem(final Item ITEM) {
        if (!items.contains(ITEM)) {
            items.add(ITEM);
            redraw();
        }
    }
    public void addItems(final Item... ITEMS) {
        addItems(Arrays.asList(ITEMS));
    }
    public void addItems(final List<Item> ITEMS) {
        ITEMS.forEach(item -> addItem(item));
    }
    public void removeItem(final Item ITEM) {
        if (items.contains(ITEM)) {
            items.remove(ITEM);
            redraw();
        }
    }
    public void removeItems(final Item... ITEMS) {
        removeItems(Arrays.asList(ITEMS));
    }
    public void removeItems(final List<Item> ITEMS) {
        ITEMS.forEach(item -> removeItem(item));
    }

    public void sortItemsAscending() {
        Collections.sort(items, Comparator.comparingDouble(Item::getValue));
        redraw();
    }
    public void sortItemsDescending() {
        Collections.sort(items, Comparator.comparingDouble(Item::getValue).reversed());
        redraw();
    }

    public double sumOfAllItems() { return items.stream().mapToDouble(Item::getValue).sum(); }

    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
            redraw();
        } else {
            textColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) {
            textColor = new ObjectPropertyBase<Color>(_textColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return CoxcombChart.this; }
                @Override public String getName() { return "textColor"; }
            };
            _textColor = null;
        }
        return textColor;
    }

    public boolean isAutoTextColor() { return null == autoTextColor ? _autoTextColor : autoTextColor.get(); }
    public void setAutoTextColor(final boolean AUTO) {
        if (null == autoTextColor) {
            _autoTextColor = AUTO;
            redraw();
        } else {
            autoTextColor.set(AUTO);
        }
    }
    public BooleanProperty autoTextColorProperty() {
        if (null == autoTextColor) {
            autoTextColor = new BooleanPropertyBase(_autoTextColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return CoxcombChart.this; }
                @Override public String getName() { return "autoTextColor"; }
            };
        }
        return autoTextColor;
    }

    private void drawChart() {
        int          noOfItems   = items.size();
        double       center      = size * 0.5;
        double       barWidth    = size * 0.04;
        double       sum         = sumOfAllItems();
        double       stepSize    = 360.0 / sum;
        double       angle       = 0;
        double       startAngle  = 90;
        double       xy          = size * 0.32;
        double       minWH       = size * 0.36;
        double       maxWH       = size * 0.64;
        double       wh          = minWH;
        double       whStep      = (maxWH - minWH) / noOfItems;
        Color        textColor   = getTextColor();
        boolean      isAutoColor = isAutoTextColor();
        DropShadow   shadow      = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.75), size * 0.02, 0, 0, 0);
        double       spread      = size * 0.005;
        double       x, y;
        double       tx, ty;
        double       endAngle;
        double       radius;
        double       clippingRadius;

        ctx.clearRect(0, 0, size, size);
        ctx.setFont(Font.font(size * 0.03));
        for (int i = 0 ; i < noOfItems ; i++) {
            Item   item  = items.get(i);
            double value = item.getValue();
            String name = item.getName();

            startAngle += angle;
            xy         -= (whStep / 2.0);
            wh         += whStep;
            barWidth   += whStep;

            angle          = value * stepSize;
            endAngle       = startAngle + angle;
            radius         = wh * 0.5;
            clippingRadius = radius + barWidth * 0.5;

            // Set Segment Clipping
            ctx.save();
                ctx.beginPath();
                ctx.arc(center, center, clippingRadius, clippingRadius, 0, 360);
                ctx.clip();

                // Segment
                ctx.save();
                    // Draw segment
                    ctx.setLineWidth(barWidth);
                    ctx.setStroke(item.getColor());
                    ctx.strokeArc(xy, xy, wh, wh, startAngle, angle, ArcType.OPEN);
                    // Add shadow effect to segment
                    if (i != (noOfItems-1) && angle > 2) {
                        x = Math.cos(Math.toRadians(endAngle - 5));
                        y = -Math.sin(Math.toRadians(endAngle - 5));
                        shadow.setOffsetX(x * spread);
                        shadow.setOffsetY(y * spread);
                        ctx.save();
                            ctx.setEffect(shadow);
                            ctx.strokeArc(xy, xy, wh, wh, endAngle, 2, ArcType.OPEN);
                        ctx.restore();
                        if (i == 0) {
                            x = Math.cos(Math.toRadians(startAngle + 5));
                            y = -Math.sin(Math.toRadians(startAngle + 5));
                            shadow.setOffsetX(x * spread);
                            shadow.setOffsetY(y * spread);
                            ctx.setEffect(shadow);
                            ctx.strokeArc(xy, xy, wh, wh, startAngle, -2, ArcType.OPEN);
                        }
                    }
                ctx.restore();

            // Remove Segment Clipping
            ctx.restore();

            // Percentage
            if (angle > 8) {
                tx = center + radius * Math.cos(Math.toRadians(endAngle - angle * 0.5));
                ty = center - radius * Math.sin(Math.toRadians(endAngle - angle * 0.5));
                if (isAutoColor) {
                    ctx.setFill(Color.WHITE);
                } else {
                    ctx.setFill(Color.WHITE);
                }
                ctx.fillText(String.format(Locale.US, "%.0f%%", (value / sum * 100.0)), tx, ty, barWidth);
                ctx.setFill(Color.BLACK);
                ctx.fillText(name, tx, ty - 20);
            }
        }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            canvas.setWidth(size);
            canvas.setHeight(size);

            redraw();
        }
    }

    private void redraw() {
        drawChart();
    }
}
