import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows the user to load an image and to resize and move it.
 * The image is cropped by the given viewport.
 */
public final class ImageManipulator extends VBox {

    private double scrollX, scrollY;

    private DoubleProperty scale = new SimpleDoubleProperty();

    private IntegerProperty offsetX = new SimpleIntegerProperty();

    private IntegerProperty offsetY = new SimpleIntegerProperty();

    /**
     *
     */
    public ImageManipulator() {

        minWidthProperty().bind(new DoubleBinding() {
            {
                super.bind(viewportWidth);
            }

            @Override
            protected double computeValue() {
                return viewportWidth.get() + 2;
            }
        });
        maxWidthProperty().bind(minWidthProperty());
        setSpacing(2);

        // ImageView
        ImageView imageView = new ImageView();
        imageView.cursorProperty().bind(new ObjectBinding<Cursor>() {
            {
                super.bind(editMode, canEdit);
            }

            @Override
            protected Cursor computeValue() {
                return editMode.get() && canEdit.get() ? Cursor.MOVE : Cursor.DEFAULT;
            }
        });
        imageView.imageProperty().bind(image);


        // ScrollPane
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(imageView);
        scrollPane.prefViewportHeightProperty().bind(viewportHeightProperty());
        scrollPane.prefViewportWidthProperty().bind(viewportWidthProperty());
        scrollPane.pannableProperty().bind(canEdit.and(editMode));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Slider
        final Slider slider = new Slider();
        slider.setMax(1);
        slider.minProperty().bind(new DoubleBinding() {
            {
                super.bind(image, scrollPane.prefViewportHeightProperty(), scrollPane.prefViewportWidthProperty());
            }

            @Override
            protected double computeValue() {
                if (image.get() != null) {
                    if (image.get().getHeight() < image.get().getWidth()) {
                        return scrollPane.getPrefViewportHeight() / image.get().getHeight();
                    } else {
                        return scrollPane.getPrefViewportWidth() / image.get().getWidth();
                    }
                } else {
                    return 1;
                }
            }
        });
        // Start with the minimal image.
        slider.setValue(slider.getMin());
        slider.visibleProperty().bind(canEdit.and(editMode));

        canEdit.bind(new BooleanBinding() {
            {
                super.bind(image);
            }

            @Override
            protected boolean computeValue() {
                if (image.get() != null) {
                    return image.get().getWidth() != viewportWidth.get() || image.get().getHeight() != viewportHeight.get();
                }
                return false;
            }
        });

        // ScaleBinding for image view.
        class ScaleBinding extends DoubleBinding {
            private boolean horizontal;

            private ScaleBinding(boolean horizontal) {
                super.bind(image, slider.valueProperty());
                this.horizontal = horizontal;
            }

            @Override
            protected double computeValue() {
                // scale the image according to the slider value.
                if (image.get() != null) {
                    return (horizontal ? image.get().getWidth() : image.get().getHeight()) * slider.getValue();
                } else {
                    return 1;
                }
            }
        }

        // TranslateBinding for image view.
        class TranslateBinding extends DoubleBinding {
            private boolean horizontal;

            private TranslateBinding(boolean horizontal) {
                super.bind(image, slider.valueProperty());
                this.horizontal = horizontal;
            }

            @Override
            protected double computeValue() {
                if (image.get() != null) {
                    double a = horizontal ? image.get().getWidth() : image.get().getHeight();
                    double b = horizontal ? scrollPane.prefViewportWidthProperty().get() : scrollPane.prefViewportHeightProperty().get();
                    // If the scaled image is smaller than the viewport, than translate it to center (top left corner is (viewport - image size) / 2)
                    if (a * slider.getValue() < b) {
                        return (b - a * slider.getValue()) / 2.0;
                    } else {
                        // If the image is larger than the scroll pane, scroll to the last known scroll position and don't translate it.
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (horizontal) {
                                    scrollPane.setHvalue(scrollX);
                                } else {
                                    scrollPane.setVvalue(scrollY);
                                }
                            }
                        });
                    }
                }
                return 0;
            }
        }

        // Don't use scaling here, since this won't affect the scroll area.
        imageView.fitWidthProperty().bind(new ScaleBinding(true));
        imageView.fitHeightProperty().bind(new ScaleBinding(false));

        imageView.translateXProperty().bind(new TranslateBinding(true));
        imageView.translateYProperty().bind(new TranslateBinding(false));

        // When the mouse wheel is used, scale the image, instead of scrolling the content.
        scrollPane.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                if (slider.isVisible()) {
                    slider.setValue(slider.getValue() + scrollEvent.getDeltaY() * 0.001);
                }
                // Prevent default behavior (scrolling).
                scrollEvent.consume();
            }
        });

        scrollPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                scrollX = scrollPane.getHvalue();
                scrollY = scrollPane.getVvalue();
            }
        });

        scale.bind(slider.valueProperty());

        offsetX.bind(new IntegerBinding() {
            {
                super.bind(viewportWidth, image, scrollPane.hvalueProperty());
            }

            @Override
            protected int computeValue() {
                if (image.get() != null) {
                    return (int) (scrollPane.getHvalue() * (scale.get() * image.get().getWidth() - viewportWidth.get()));
                }
                return 0;
            }
        });

        offsetY.bind(new IntegerBinding() {
            {
                super.bind(viewportHeight, image, scrollPane.vvalueProperty());
            }

            @Override
            protected int computeValue() {
                if (image.get() != null) {
                    return (int) (scrollPane.getVvalue() * (scale.get() * image.get().getHeight() - viewportHeight.get()));
                }
                return 0;
            }
        });

        image.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        scrollPane.setHvalue(0.5);
                        scrollPane.setVvalue(0.5);
                        scrollX = scrollPane.getHvalue();
                        scrollY = scrollPane.getVvalue();
                        slider.setValue(slider.getMin());
                    }
                });
            }
        });

        getChildren().add(scrollPane);
        getChildren().add(slider);
    }

    private ObjectProperty<byte[]> imageData = new SimpleObjectProperty<byte[]>();

    /**
     * The image data of the resized and cropped image.
     *
     * @return The property.
     */
    public ReadOnlyObjectProperty<byte[]> imageDataProperty() {
        return imageData;
    }

    private ObjectProperty<Image> image = new SimpleObjectProperty<Image>();

    /**
     * The image, which can either by set by the {@link #chooseFile} method, or from outside.
     *
     * @return The property.
     */
    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    private IntegerProperty viewportWidth = new SimpleIntegerProperty(150);

    /**
     * The viewport width of the scroll area and the width of the resulting image.
     *
     * @return The property.
     */
    public IntegerProperty viewportWidthProperty() {
        return viewportWidth;
    }

    private IntegerProperty viewportHeight = new SimpleIntegerProperty(150);

    /**
     * The viewport height of the scroll area and the height of the resulting image.
     *
     * @return The property.
     */
    public IntegerProperty viewportHeightProperty() {
        return viewportHeight;
    }

    private BooleanProperty canEdit = new SimpleBooleanProperty();

    /**
     * True, if the image can be edited, that is, if its size differs from viewport size.
     *
     * @return The property.
     */
    public ReadOnlyBooleanProperty canEditProperty() {
        return canEdit;
    }

    private BooleanProperty editMode = new SimpleBooleanProperty();

    /**
     * Indicates whether it is the user allowed to edit the image.
     *
     * @return The property.
     */
    public BooleanProperty editModeProperty() {
        return editMode;
    }


    /**
     * Chooses an image file.
     *
     * @return The chosen file, or null, if none was chosen.
     */
    public File chooseFile() {
        FileChooser fileChooser = new FileChooser();
        List<String> imageFormats = new ArrayList<String>();
        imageFormats.add("*.jpg");
        imageFormats.add("*.png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.jpg, *.png", imageFormats));

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                image.set(new Image(new FileInputStream(file)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return file;
        }
        return null;
    }

    /**
     * Finishes the editing of the image and computes the new image.
     * Eventually the {@link #imageData} is set with the new image data.
     */
    /*public void finish() {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageProperty().get(), null);
            int newWidth = (int) (bufferedImage.getWidth() * scale.get());
            int newHeight = (int) (bufferedImage.getHeight() * scale.get());
            BufferedImage scaledImage = Scalr.resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, newWidth, newHeight);
            BufferedImage croppedImage = Scalr.crop(scaledImage, offsetX.get(), offsetY.get(), viewportWidthProperty().get(), viewportHeightProperty().get());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(croppedImage, "png", outputStream);
            outputStream.close();
            byte[] data = outputStream.toByteArray();
            imageData.set(data);
            image.set(new Image(new ByteArrayInputStream(data)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}