package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.User;

public class UserListView extends BorderPane {
    
    private ObservableList<User> users;
    private FilteredList<User> filteredUsers;
    private ListView<User> userList;
    private TextField searchField;
    private Label titleLabel;
    private Label countLabel;

    public UserListView() {
        initializeUI();
    }
    
    private void initializeUI() {
        this.setStyle("-fx-background-color: white; -fx-border-color: #e9edef; -fx-border-width: 1 0 0 0;");
        this.setPrefWidth(320);
        
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("-fx-background-color: white;");
        
        HBox headerBox = createHeader();
        searchField = createSearchField();
        
        users = FXCollections.observableArrayList();
        filteredUsers = new FilteredList<>(users, p -> true);
        userList = createUserList();
        
        mainContainer.getChildren().addAll(headerBox, searchField, userList);
        VBox.setVgrow(userList, Priority.ALWAYS);
        
        this.setCenter(mainContainer);
    }
    
    private HBox createHeader() {
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 5, 5));
        
        titleLabel = new Label("👥 Contacts");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #00a884;");
        
        countLabel = new Label("0 contacts");
        countLabel.setFont(Font.font(12));
        countLabel.setStyle("-fx-text-fill: #667781;");
        
        HBox rightBox = new HBox(countLabel);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(titleLabel, rightBox);
        
        return headerBox;
    }
    
    private TextField createSearchField() {
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher un contact...");
        searchField.setStyle(
            "-fx-background-color: #f0f2f5;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 8 15;" +
            "-fx-font-size: 13px;"
        );
        
        Tooltip searchTooltip = new Tooltip("Tapez le nom d'un contact");
        searchField.setTooltip(searchTooltip);
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredUsers.setPredicate(user -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return user.getName().toLowerCase().contains(lowerCaseFilter);
            });
            updateCount();
        });
        
        return searchField;
    }
    
    private ListView<User> createUserList() {
        ListView<User> listView = new ListView<>();
        listView.setItems(filteredUsers);
        listView.setCellFactory(lv -> new UserCell());
        listView.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;"
        );
        
        return listView;
    }
    
    private void updateCount() {
        int total = users.size();
        int displayed = filteredUsers.size();
        
        if (displayed < total) {
            countLabel.setText(displayed + " sur " + total + " contacts");
        } else {
            countLabel.setText(total + " contacts");
        }
    }
    
    private class UserCell extends ListCell<User> {
        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            
            if (empty || user == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(12);
                container.setPadding(new Insets(8, 10, 8, 10));
                container.setAlignment(Pos.CENTER_LEFT);
                container.setStyle("-fx-background-color: transparent;");
                
                // Avatar avec initiale
                Label avatarLabel = new Label(getInitial(user.getName()));
                avatarLabel.setMinSize(45, 45);
                avatarLabel.setMaxSize(45, 45);
                avatarLabel.setAlignment(Pos.CENTER);
                avatarLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
                avatarLabel.setStyle(
                    "-fx-background-color: #6c757d;" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;"
                );
                
                VBox infoBox = new VBox(3);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                
                Label nameLabel = new Label(user.getName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                nameLabel.setStyle("-fx-text-fill: #111b21;");
                
                Label statusLabel = new Label("Contact");
                statusLabel.setFont(Font.font(12));
                statusLabel.setStyle("-fx-text-fill: #667781;");
                
                infoBox.getChildren().addAll(nameLabel, statusLabel);
                
                container.getChildren().addAll(avatarLabel, infoBox);
                HBox.setHgrow(infoBox, Priority.ALWAYS);
                
                // Effet au survol
                container.setOnMouseEntered(e -> {
                    if (!isSelected()) {
                        container.setStyle("-fx-background-color: #f5f6f6; -fx-background-radius: 10;");
                    }
                });
                
                container.setOnMouseExited(e -> {
                    if (!isSelected()) {
                        container.setStyle("-fx-background-color: transparent;");
                    }
                });
                
                // Style pour l'élément sélectionné
                if (isSelected()) {
                    container.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 10; -fx-border-color: #00a884; -fx-border-radius: 10; -fx-border-width: 1;");
                }
                
                setGraphic(container);
            }
        }
        
        private String getInitial(String name) {
            return name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        }
    }
    
    // ========== MÉTHODES PUBLIQUES ==========
    
    public User getSelectedUser() {
        return userList.getSelectionModel().getSelectedItem();
    }
    
    public void addUser(User user) {
        users.add(user);
        updateCount();
    }
    
    public void addUser(String username) {
        users.add(new User(username));
        updateCount();
    }
    
    public void removeUser(String username) {
        users.removeIf(user -> user.getName().equals(username));
        updateCount();
    }
    
    public void removeUser(User user) {
        users.remove(user);
        updateCount();
    }
    
    public void setOnUserSelected(java.util.function.Consumer<User> handler) {
        if (userList != null) {
            userList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        handler.accept(newVal);
                    }
                }
            );
        }
    }
    
    public ObservableList<User> getUsers() {
        return users;
    }
    
    // Méthode pour vider la recherche
    public void resetSearch() {
        searchField.clear();
    }
}