package secondLab;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.atteo.classindex.ClassIndex;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class Initializer {
    static final String FXML_PATH = "fxml/";
    private static final Class<ConnectableItem> annotationClass = ConnectableItem.class;
    private final Stage ownerStage;
    private final Function<Class<?>, OpenableWindow> initializeWindowController = new Function<>() {
        @Override
        public OpenableWindow apply(Class<?> clazz) {
            OpenableWindow controller = null;
            try {
                controller = (OpenableWindow) clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            String path = FXML_PATH + controller.getClass().getDeclaredAnnotation(annotationClass).pathFXML();
            controller = initializeModalityWindow(path, controller);
            controller.getStage().initOwner(ownerStage);
            controller.getStage().setTitle(controller.getClass().getDeclaredAnnotation(annotationClass).name());
            return controller;
        }
    };

    public Initializer(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    private static <T extends OpenableWindow> T initializeModalityWindow(String pathFXML, T modalityWindow) {
        FXMLLoader loader;
        Parent createNewFunction;
        Stage createNewFunctionStage = new Stage();
        try {
            loader = new FXMLLoader(modalityWindow.getClass().getClassLoader().getResource(pathFXML));
            createNewFunction = loader.load();
            modalityWindow = loader.getController();
            createNewFunctionStage.setScene(new Scene(createNewFunction));
            createNewFunctionStage.initModality(Modality.APPLICATION_MODAL);
            modalityWindow.setStage(createNewFunctionStage);
        } catch (IOException e) {
            AlertWindows.showError(e);
        }
        return modalityWindow;
    }

    public void initializeWindowControllers(Map<String, OpenableWindow> controllerMap) {
        StreamSupport.stream(ClassIndex.getAnnotated(annotationClass).spliterator(), false)
                .filter(f -> f.getDeclaredAnnotation(annotationClass).type() == Item.CONTROLLER)
                .forEach(clazz -> controllerMap.put(clazz.getDeclaredAnnotation(annotationClass).pathFXML(),
                        initializeWindowController.apply(clazz)));
    }
}