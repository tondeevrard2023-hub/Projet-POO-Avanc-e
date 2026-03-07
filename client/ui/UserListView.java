package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.User;

public class UserListView extends BorderPane {
    
    private ObservableList<User> users;
    private ListView<User> userList;
    private TextField searchField;
    private Label titleLabel;
    private Label countLabel;

    // Constructeur - Sans utilisateurs par défaut
    public UserListView() {
        initializeUI();
        // Plus d'appel à loadDemoUsers() - Le serveur ajoutera les utilisateurs
    }
    
    private void initializeUI() {
        // Style général
        this.setStyle("-fx-background-color: white; -fx-border-color: #e9edef; -fx-border-width: 1 0 0 0;");
        this.setPrefWidth(320);
        
        // Panneau principal
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("-fx-background-color: white;");
        
        // En-tête
        HBox headerBox = createHeader();
        
        // Barre de recherche
        searchField = createSearchField();
        
        // Liste des utilisateurs (vide au départ)
        users = FXCollections.observableArrayList();
        userList = createUserList();
        
        mainContainer.getChildren().addAll(headerBox, searchField, userList);
        VBox.setVgrow(userList, Priority.ALWAYS);
        
        this.setCenter(mainContainer);
    }
    
    private HBox createHeader() {
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 5, 5));
        
        titleLabel = new Label("Contacts");
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
        searchField.setPromptText("Rechercher un contact...");
        searchField.setStyle(
            "-fx-background-color: #f0f2f5;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 8 15;" +
            "-fx-font-size: 13px;"
        );
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));
        
        return searchField;
    }
    
    private ListView<User> createUserList() {
        ListView<User> listView = new ListView<>();
        listView.setItems(users);
        listView.setCellFactory(lv -> new UserCell());
        listView.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;"
        );
        
        return listView;
    }
    
    private void filterUsers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            userList.setItems(users);
        } else {
            ObservableList<User> filtered = FXCollections.observableArrayList();
            for (User user : users) {
                if (user.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    filtered.add(user);
                }
            }
            userList.setItems(filtered);
        }
    }
    
    private void updateCount() {
        countLabel.setText(users.size() + " contacts");
    }
    
    // Cellule personnalisée pour afficher un utilisateur
    private class UserCell extends ListCell<User> {
        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            
            if (empty || user == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Conteneur principal
                HBox container = new HBox(12);
                container.setPadding(new Insets(8, 10, 8, 10));
                container.setAlignment(Pos.CENTER_LEFT);
                container.setStyle("-fx-background-color: transparent;");
                
                // Avatar avec initiale
                Label avatarLabel = new Label(getInitial(user.getName()));
                avatarLabel.setMinSize(40, 40);
                avatarLabel.setMaxSize(40, 40);
                avatarLabel.setAlignment(Pos.CENTER);
                avatarLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                avatarLabel.setStyle(
                    "-fx-background-color: #6c757d;" +
                    "-fx-background-radius: 20;" +
                    "-fx-text-fill: white;"
                );
                
                // Informations utilisateur
                VBox infoBox = new VBox(3);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                
                // Nom
                Label nameLabel = new Label(user.getName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                nameLabel.setStyle("-fx-text-fill: #111b21;");
                
                // Statut (sera mis à jour par le serveur plus tard)
                Label statusLabel = new Label("Hors ligne");
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
                
                setGraphic(container);
            }
        }
        
        private String getInitial(String name) {
            return name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        }
    }
    
    // Méthodes publiques pour le serveur
    
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
    
    // Méthode pour mettre à jour le statut d'un utilisateur
    public void updateUserStatus(String username, boolean isOnline) {
        for (User user : users) {
            if (user.getName().equals(username)) {
                // Tu pourras ajouter un champ "online" dans User plus tard
                // Pour l'instant, on ne fait rien
                break;
            }
        }
        userList.refresh();
    }
}