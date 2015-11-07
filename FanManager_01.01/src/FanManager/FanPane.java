/*
 * Notes: 
 *  Element positioning is pretty weak. Probably need a more robust system.
 *  No fan animation currently
 *  Not connected to the fan object yet.
 *  temperatureTF event handler not setup yet.
 *  Knob not added in yet.
 */
package FanManager;

import FanManager.model.Fan;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Felix
 */
public class FanPane extends FlowPane {

    private static final double WIDTH = 343, HEIGHT = 768;
    private static int nextId = 0;
    private int id;
    
    // Gauge
    private static final Random RND = new Random();
    private Section[]           sections;
    private long                lastTimerCall;
    private AnimationTimer      timer;
    private GaugeObject         gauge;

    // SpeedInputField
    private SpeedInputField     speedField;

//    // Label
//    private SpeedInputLabel     speedLabel;
 
    // Slider
    Slider                      speedSlider;
    
    // Label
    final private Label text = new Label();


    // Fan
    private Fan                 fan;

    TextField                   temperatureTF;

    public FanPane() {
        // Create an individual ID for Fan Pane
        id = ++nextId;

        // Create a maximum width and height for predictable columns
        setMaxWidth(WIDTH);
        setMaxHeight(HEIGHT);

        // Create fan object
        fan = new Fan();

        // Create gauge
        gauge = gauge();
        gauge.setTranslateX((int) (WIDTH / 24));
        gauge.setTranslateY((int) (HEIGHT / 32));
        gauge.setStyle("-fx-background-color: transparent");

        
        // Create speed input field
        speedField = SpeedInputField();
//        // Create speed input label
//        speedLabel = SpeedInputLabel();
        

        // Create and style temperature label
        Label temperatureLabel = new Label("Temperature");
        temperatureLabel.setTranslateX((int) (WIDTH / 2) - 150);
        temperatureLabel.setTranslateY((int) (HEIGHT / 4) + 200);
        temperatureLabel.setStyle("-fx-background-color: green");

        // Create temperature text field
        temperatureTF = new TextField();
        temperatureTF.setTranslateX((int) (WIDTH / 2) - 70);
        temperatureTF.setTranslateY((int) (HEIGHT / 4) + 200);
        temperatureTF.setText("" + fan.getTemperature());

        // Create and style speed label
        Label speedLabel = new Label("Speed");
        speedLabel.setTranslateX((int) (WIDTH / 2) - 150);
        speedLabel.setTranslateY((int) (HEIGHT / 4) + 240);
        speedLabel.setStyle("-fx-background-color: green");

        // Create speed slider
        speedSlider = new Slider(0, 100, 0);
        speedSlider.setTranslateX((int) (WIDTH / 2) - 70);
        speedSlider.setTranslateY((int) (HEIGHT / 4) + 240);

        // Locate speedField
        speedField.setTranslateX((int) (WIDTH / 16));
        speedField.setTranslateY((int) (HEIGHT / 32));
        speedField.valueProperty().bindBidirectional(gauge.valueProperty());
        speedField.valueProperty().bindBidirectional(speedSlider.valueProperty());
        speedField.setPrefWidth(75);
        

        // Create label
        text.setStyle("-fx-color: white");    
        text.setStyle("-fx-font: 12 arial;"); 
        text.setStyle("-fx-background-color: grey");
        text.setTranslateX((int) (WIDTH / 16));
        text.setTranslateY((int) (HEIGHT / 32 + 300));

        text.setText(Math.round(speedSlider.getValue()) + "");
        text.setText(Math.round(gauge.getValue()) + "");
        gauge.valueProperty().addListener(new ChangeListener<Number>() {
          @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
            if (newValue == null) {
              text.setText("");
              return;
            }
        text.setText((newValue.intValue()) + "");
        }
    });
     speedSlider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
        if (newValue == null) {
          text.setText("");
          return;
        }
        text.setText(Math.round(newValue.intValue()) + "");
      }
    });   

    text.setPrefWidth(50);        // allow the label to be clicked on to display an editable text field and have a slider movement cancel any current edit.
    text.setOnMouseClicked(newLabelEditHandler(text, speedSlider));
    speedSlider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
        text.setContentDisplay(ContentDisplay.TEXT_ONLY);
      }
    });
//        setTextAndValueProperty(gauge, text, sliderObject);


        // Create background fill
        Rectangle background = new Rectangle(WIDTH, HEIGHT, Color.BLACK);
        background.setStroke(Color.WHITE);

        // create main content
        Group group = new Group(
                background,
                gauge,
                temperatureLabel,
                temperatureTF,
                speedLabel,
                speedSlider,
                speedField,
                text
        );

        // Add main content to pane
        getChildren().add(group);
        // Event handler: 
        //   Inputs:    Temperature Data
        //   Performs:  Turn On/Off/Changes Speed of Fan animation
        //   Outputs:   None
        temperatureTF.setOnAction((ActionEvent e) -> {
            System.out.println(id + ": " + temperatureTF.getText());

            // Update Temperature
            fan.setTemperature(Double.parseDouble(temperatureTF.getText()));

        });

        // Event handler: 
        //   Inputs:    Fan Speed
        //   Performs:  Turn On/Off/Changes Speed of Fan animation
        //   Outputs:   None

         speedSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {

                System.out.println(id + ": " + speedSlider.getValue());

                // Update Fan Speed
                fan.setSpeed(speedSlider.getValue());
            }
        });
    }


    // Fan
    public Fan getFan() {
        return fan;
    }

    public void setFan(Fan fan) {
        this.fan = fan;
        temperatureTF.setText("" + fan.getTemperature());
        speedSlider.setValue(fan.getSpeed());
    }

    

// ******************** Constructors **************************************

    /**
     *
     * @return 
     */
        public final GaugeObject gauge() {
        sections = new Section[] {
            new Section(0, 9.9, Color.rgb(64, 182, 75)),
            new Section(9.9, 10.1, Color.rgb(255, 255, 255)),
            new Section(10.1, 19.9, Color.rgb(64, 182, 75)),
            new Section(19.9, 20.1, Color.rgb(255, 255, 255)),
            new Section(20.1, 29.9, Color.rgb(64, 182, 75)),
            new Section(29.9, 30.1, Color.rgb(255, 255, 255)),
            new Section(30.1, 39.9, Color.rgb(64, 182, 75)),
            new Section(39.9, 40.1, Color.rgb(255, 255, 255)),
            new Section(40.1, 49.9, Color.rgb(64, 182, 75)),
            new Section(49.9, 50.1, Color.rgb(255, 255, 255)),
            new Section(50.1, 59.9, Color.rgb(64, 182, 75)),
            new Section(59.9, 60.1, Color.rgb(255, 255, 255)),
            new Section(60.1, 69.9, Color.rgb(64, 182, 75)),
            new Section(69.9, 70.1, Color.rgb(255, 255, 255)),
            new Section(70.1, 79.9, Color.rgb(64, 182, 75)),
            new Section(79.9, 80.1, Color.rgb(255, 255, 255)),
            new Section(80.1, 89.9, Color.rgb(209, 184, 74)),
            new Section(89.9, 90.1, Color.rgb(255, 255, 255)),
            new Section(90.1, 99.9, Color.rgb(209, 78, 74)),
            new Section(99.9, 100, Color.rgb(255, 255, 255))
//            new Section(0, 100, Color.rgb(64, 182, 75))
                
        };
        
        gauge = new GaugeObject();
        gauge.setMinValue(0);
        gauge.setMaxValue(100);
        gauge.setThreshold(80);
        gauge.setSections(sections);
        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 3_000_000_000l) {
                    gauge.setValue(RND.nextDouble() * 30.0 + 10);
                    lastTimerCall = now;
                }
            }
        };
        
        return gauge;

        };

    private SpeedInputField SpeedInputField() {
        speedField = new SpeedInputField(0, 100, 0);

        return speedField;
    }
    
//    private SpeedInputLabel SpeedInputLabel() {
////        speedLabel = new SpeedInputLabel(sliderObject);
//        
//    }


// helper eventhandler that allows a text label to be edited within the range of a slider.
  private EventHandler<MouseEvent> newLabelEditHandler(final Label text, final GaugeObject gauge) {
    return new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        final TextField editField = new TextField(text.getText());
        text.setGraphic(editField);
        text.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        editField.setTranslateX(-text.getGraphicTextGap() - 1);
        editField.setTranslateY(-3); // adjustment hack to get alignment right.
        editField.requestFocus();
        editField.selectAll(); // hmm there is a weird bug in javafx here, the text is not selected,
                               // but if I focus on another window by clicking only on title bars,
                               // then back to the javafx app, the text is magically selected.
 
        editField.setOnKeyReleased(new EventHandler<KeyEvent>() {
          @Override public void handle(KeyEvent t) {
            if (t.getCode() == KeyCode.ENTER) {
              text.setContentDisplay(ContentDisplay.TEXT_ONLY);
 
              // field is empty, cancel the edit.
              if (editField.getText() == null || editField.getText().equals("")) {
                return;
              }
 
              try {
                double editedValue = Double.parseDouble(editField.getText());
                if (gauge.getMinValue() <= editedValue && editedValue <= gauge.getMaxValue()) {
                  // edited value was within in the valid slider range, perform the edit.
                  gauge.setValue(Integer.parseInt(editField.getText()));
                  text.setText(editField.getText());
                }
              } catch (NumberFormatException e) { // a valid numeric value was not entered,
                text.setContentDisplay(ContentDisplay.TEXT_ONLY);
              }
            } else if (t.getCode() == KeyCode.ESCAPE) {
              text.setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
          }
        });
      }
    };
  }
// helper eventhandler that allows a text label to be edited within the range of a slider.
  private EventHandler<MouseEvent> newLabelEditHandler(final Label text, final Slider slider) {
    return new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        final TextField editField = new TextField(text.getText());
        text.setGraphic(editField);
        text.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        editField.setTranslateX(-text.getGraphicTextGap() - 1);
        editField.setTranslateY(-3); // adjustment hack to get alignment right.
        editField.requestFocus();
        editField.selectAll(); // hmm there is a weird bug in javafx here, the text is not selected,
                               // but if I focus on another window by clicking only on title bars,
                               // then back to the javafx app, the text is magically selected.
 
        editField.setOnKeyReleased(new EventHandler<KeyEvent>() {
          @Override public void handle(KeyEvent t) {
            if (t.getCode() == KeyCode.ENTER) {
              text.setContentDisplay(ContentDisplay.TEXT_ONLY);
 
              // field is empty, cancel the edit.
              if (editField.getText() == null || editField.getText().equals("")) {
                return;
              }
 
              try {
                double editedValue = Double.parseDouble(editField.getText());
                if (slider.getMin() <= editedValue && editedValue <= slider.getMax()) {
                  // edited value was within in the valid slider range, perform the edit.
                  slider.setValue(Integer.parseInt(editField.getText()));
                  text.setText(editField.getText());
                }
              } catch (NumberFormatException e) { // a valid numeric value was not entered,
                text.setContentDisplay(ContentDisplay.TEXT_ONLY);
              }
            } else if (t.getCode() == KeyCode.ESCAPE) {
              text.setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
          }
        });
      }
    };
  }

//  void setTextAndValueProperty(GaugeObject gauge, Label text, Slider sliderObject)
//  {
//    text.setStyle("-fx-color: white");    
//    text.setStyle("-fx-font: 24 arial;");    
//    text.setText(Math.round(sliderObject.getValue()) + "");
//    text.setText(Math.round(gauge.getValue()) + "");
//    
//      gauge.valueProperty().addListener(new ChangeListener<Number>() {
//      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
//        if (newValue == null) {
//          text.setText("");
//          return;
//        }
//        text.setText((newValue.intValue()) + "");
//      }
//    });
//    sliderObject.valueProperty().addListener(new ChangeListener<Number>() {
//      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
//        if (newValue == null) {
//          text.setText("");
//          return;
//        }
//        text.setText(Math.round(newValue.intValue()) + "");
//      }
//    });
//    
//    text.setPrefWidth(50);
//    
//    text.setOnMouseClicked(newLabelEditHandler(text, sliderObject));
//    sliderObject.valueProperty().addListener(new ChangeListener<Number>() {
//      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
//        text.setContentDisplay(ContentDisplay.TEXT_ONLY);
//      }
//    });
//  }


}
