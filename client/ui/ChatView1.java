package ui;

import java.time.LocalTime;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import model.Message;
import model.MessageFile;
import model.MessageText;
import model.User;
import ui.UserListView;

public class ChatView1 extends BorderPane {

    // Liste des conversations
    private ListView<ConversationItem> conversationListView;
    private ObservableList<ConversationItem> conversations;
    
    // Zone de chat principale
    private ListView<Message> messageListView;
    private ObservableList<Message> messages;
    
    // Zone de saisie
    private TextArea messageArea;
    private TextField inputField;
    private Button sendButton;
    private Button fileButton;
    private Button emojiButton;
    private Button quitButton;
    
    // En-tête de conversation
    private Label conversationTitleLabel;
    private Label conversationStatusLabel;
    private Button infoButton;
    
    // Conversation actuelle
    private ConversationItem currentConversation;
    
    // Déclarer UserListView
    private UserListView userListView;
    
    // ========== NOUVELLES VARIABLES POUR LES AMÉLIORATIONS ==========
    private Label connectionStatusLabel;
    private Label onlineCountLabel;
    private Button settingsButton;
    
    // Types de conversations
    public enum ConversationType {
        PUBLIC_GROUP, PRIVATE
    }
    
    // Classe interne pour représenter un élément de conversation
    public static class ConversationItem {
        private String id;
        private String name;
        private ConversationType type;
        private String lastMessage;
        private LocalTime lastMessageTime;
        private int unreadCount;
        private User otherUser;
        
        public ConversationItem(String id, String name, ConversationType type) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.lastMessage = "";
            this.lastMessageTime = LocalTime.now();
            this.unreadCount = 0;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public ConversationType getType() { return type; }
        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public LocalTime getLastMessageTime() { return lastMessageTime; }
        public void setLastMessageTime(LocalTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
        public int getUnreadCount() { return unreadCount; }
        public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
        public void incrementUnreadCount() { this.unreadCount++; }
        public User getOtherUser() { return otherUser; }
        public void setOtherUser(User otherUser) { this.otherUser = otherUser; }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    // Cellule personnalisée pour les conversations
    private class ConversationCell extends ListCell<ConversationItem> {
        @Override
        protected void updateItem(ConversationItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hbox = new HBox(10);
                hbox.setPadding(new Insets(8, 10, 8, 10));
                hbox.setAlignment(Pos.CENTER_LEFT);
                
                Label avatarLabel = new Label();
                avatarLabel.setMinSize(40, 40);
                avatarLabel.setMaxSize(40, 40);
                avatarLabel.setAlignment(Pos.CENTER);
                avatarLabel.setStyle(getAvatarStyle(item.getType()));
                
                if (item.getType() == ConversationType.PUBLIC_GROUP) {
                    avatarLabel.setText("👥");
                } else {
                    avatarLabel.setText("👤");
                }
                
                VBox infoBox = new VBox(3);
                
                Label nameLabel = new Label(item.getName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                
                Label lastMessageLabel = new Label(item.getLastMessage());
                lastMessageLabel.setFont(Font.font(12));
                lastMessageLabel.setStyle("-fx-text-fill: #667781;");
                lastMessageLabel.setMaxWidth(150);
                
                infoBox.getChildren().addAll(nameLabel, lastMessageLabel);
                
                VBox rightBox = new VBox(5);
                rightBox.setAlignment(Pos.TOP_RIGHT);
                
                Label timeLabel = new Label(formatTime(item.getLastMessageTime()));
                timeLabel.setFont(Font.font(11));
                timeLabel.setStyle("-fx-text-fill: #667781;");
                
                Label unreadLabel = new Label();
                if (item.getUnreadCount() > 0) {
                    unreadLabel.setText(String.valueOf(item.getUnreadCount()));
                    unreadLabel.setStyle(
                        "-fx-background-color: #00a884;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 2 6 2 6;" +
                        "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;"
                    );
                }
                
                rightBox.getChildren().addAll(timeLabel, unreadLabel);
                
                hbox.getChildren().addAll(avatarLabel, infoBox, rightBox);
                HBox.setHgrow(infoBox, Priority.ALWAYS);
                
                if (isSelected()) {
                    hbox.setStyle("-fx-background-color: #f0f2f5;");
                }
                
                setGraphic(hbox);
            }
        }
        
        private String getAvatarStyle(ConversationType type) {
            if (type == ConversationType.PUBLIC_GROUP) {
                return "-fx-background-color: #00a884; -fx-background-radius: 20; -fx-text-fill: white;";
            } else {
                return "-fx-background-color: #6c757d; -fx-background-radius: 20; -fx-text-fill: white;";
            }
        }
        
        private String formatTime(LocalTime time) {
            return String.format("%02d:%02d", time.getHour(), time.getMinute());
        }
    }

    public ChatView1() {
        conversations = FXCollections.observableArrayList();
        messages = FXCollections.observableArrayList();
        
        VBox leftPanel = createLeftPanel();
        BorderPane centerPanel = createCenterPanel();
        
        HBox mainLayout = new HBox(0);
        mainLayout.getChildren().addAll(leftPanel, centerPanel);
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        
        // ========== MODIFICATION : Ajout de la barre de statut ==========
        VBox rootLayout = new VBox();
        rootLayout.getChildren().addAll(mainLayout, createStatusBar());
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
        
        this.setCenter(rootLayout);
    }
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(350);
        leftPanel.setStyle("-fx-background-color: white; -fx-border-color: #e9edef; -fx-border-width: 0 1 0 0;");
        
        HBox headerBox = new HBox(10);
        headerBox.setPadding(new Insets(15, 10, 15, 10));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: #f0f2f5;");
        
        Label appTitle = new Label("Discussions");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Button newChatButton = new Button("➕");
        newChatButton.setStyle("-fx-background-color: transparent; -fx-font-size: 18;");
        newChatButton.setTooltip(new Tooltip("Nouvelle discussion"));
        
        Button menuButton = new Button("☰");
        menuButton.setStyle("-fx-background-color: transparent; -fx-font-size: 18;");
        menuButton.setTooltip(new Tooltip("Menu"));
        
        // ========== MODIFICATION : Ajout du bouton paramètres ==========
        settingsButton = new Button("⚙️");
        settingsButton.setStyle("-fx-background-color: transparent; -fx-font-size: 18;");
        settingsButton.setTooltip(new Tooltip("Paramètres"));
        settingsButton.setOnAction(e -> showSettingsDialog());
        
        HBox rightButtons = new HBox(10, newChatButton, menuButton, settingsButton);
        rightButtons.setAlignment(Pos.CENTER_RIGHT);
        
        headerBox.getChildren().addAll(appTitle, rightButtons);
        HBox.setHgrow(rightButtons, Priority.ALWAYS);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher une discussion...");
        searchField.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 20; -fx-padding: 8 15;");
        
        HBox searchBox = new HBox(searchField);
        searchBox.setPadding(new Insets(10, 10, 5, 10));
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white; -fx-tab-min-width: 120;");
        
        Tab allTab = new Tab("Toutes");
        Tab groupsTab = new Tab("Groupes");
        Tab privateTab = new Tab("Privées");
        
        tabPane.getTabs().addAll(allTab, groupsTab, privateTab);
        
        conversationListView = new ListView<>(conversations);
        conversationListView.setCellFactory(list -> new ConversationCell());
        conversationListView.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
        
        conversationListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectConversation(newVal);
                }
            }
        );
        
        Label contactsSeparator = new Label("Contacts");
        contactsSeparator.setFont(Font.font("System", FontWeight.BOLD, 14));
        contactsSeparator.setPadding(new Insets(15, 10, 5, 10));
        contactsSeparator.setStyle("-fx-text-fill: #00a884;");
        
        userListView = new UserListView();
        userListView.setPrefHeight(200);
        userListView.setStyle("-fx-background-color: white;");
        userListView.setOnUserSelected(this::handleUserSelected);
        
        VBox.setVgrow(conversationListView, Priority.ALWAYS);
        
        leftPanel.getChildren().addAll(
            headerBox, 
            searchBox, 
            tabPane, 
            conversationListView,
            contactsSeparator,
            userListView
        );
        
        VBox.setVgrow(tabPane, Priority.NEVER);
        
        return leftPanel;
    }
    
    private BorderPane createCenterPanel() {
        BorderPane centerPanel = new BorderPane();
        centerPanel.setStyle("-fx-background-color: #efeae2;");
        
        HBox chatHeader = createChatHeader();
        centerPanel.setTop(chatHeader);
        
        messageListView = new ListView<>(messages);
        messageListView.setCellFactory(list -> new MessageCell());
        messageListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        messageListView.setPadding(new Insets(10));
        
        centerPanel.setCenter(messageListView);
        
        VBox inputArea = createInputArea();
        centerPanel.setBottom(inputArea);
        
        return centerPanel;
    }
    
    private HBox createChatHeader() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #f0f2f5; -fx-border-color: #e9edef; -fx-border-width: 0 0 1 0;");
        
        conversationTitleLabel = new Label("Sélectionnez une conversation");
        conversationTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        conversationStatusLabel = new Label("");
        conversationStatusLabel.setFont(Font.font(12));
        conversationStatusLabel.setStyle("-fx-text-fill: #667781;");
        
        VBox titleBox = new VBox(2);
        titleBox.getChildren().addAll(conversationTitleLabel, conversationStatusLabel);
        
        infoButton = new Button("ℹ️");
        infoButton.setStyle("-fx-background-color: transparent; -fx-font-size: 18;");
        infoButton.setTooltip(new Tooltip("Informations de la conversation"));
        
        HBox rightBox = new HBox(infoButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        header.getChildren().addAll(titleBox, rightBox);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        
        return header;
    }
    
    private VBox createInputArea() {
        VBox inputArea = new VBox();
        inputArea.setStyle("-fx-background-color: #f0f2f5;");
        
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10, 15, 10, 15));
        inputBox.setAlignment(Pos.CENTER_LEFT);
        
        emojiButton = new Button("😊");
        emojiButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-size: 20; -fx-background-radius: 20; -fx-padding: 8 12; -fx-border-color: #e0e0e0; -fx-border-radius: 20;");
        emojiButton.setTooltip(new Tooltip("Ajouter un emoji"));
        
        inputField = new TextField();
        inputField.setPromptText("Écrivez un message...");
        inputField.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 8 15;");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        
        fileButton = new Button("📁");
        fileButton.setStyle("-fx-background-color: #00a884; -fx-text-fill: white; -fx-font-size: 20; -fx-background-radius: 20; -fx-padding: 8 12;");
        fileButton.setTooltip(new Tooltip("Joindre un fichier"));
        
        sendButton = new Button("➤");
        sendButton.setStyle("-fx-background-color: #0078FF; -fx-text-fill: white; -fx-font-size: 20; -fx-background-radius: 20; -fx-padding: 8 15;");
        sendButton.setTooltip(new Tooltip("Envoyer"));
        
        quitButton = new Button("✕");
        quitButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 15;");
        quitButton.setTooltip(new Tooltip("Quitter le chat"));
        
        inputBox.getChildren().addAll(emojiButton, inputField, fileButton, sendButton, quitButton);
        
        inputArea.getChildren().add(inputBox);
        
        return inputArea;
    }
    
    // ========== NOUVELLE MÉTHODE : Barre de statut ==========
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 15, 5, 15));
        statusBar.setStyle("-fx-background-color: #f0f2f5; -fx-border-color: #e9edef; -fx-border-width: 1 0 0 0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        connectionStatusLabel = new Label("● Connecté");
        connectionStatusLabel.setStyle("-fx-text-fill: #00a884; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        onlineCountLabel = new Label("👥 0 en ligne");
        onlineCountLabel.setStyle("-fx-text-fill: #667781; -fx-font-size: 12px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label versionLabel = new Label("v1.0.0");
        versionLabel.setStyle("-fx-text-fill: #667781; -fx-font-size: 11px;");
        
        statusBar.getChildren().addAll(connectionStatusLabel, onlineCountLabel, spacer, versionLabel);
        
        return statusBar;
    }
    
    // ========== NOUVELLE MÉTHODE : Dialogue des paramètres ==========
    private void showSettingsDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Paramètres");
        dialog.setHeaderText("Personnalisez votre expérience");
        
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // Option de thème
        Label themeLabel = new Label("Thème :");
        themeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        ToggleGroup themeGroup = new ToggleGroup();
        RadioButton lightTheme = new RadioButton("Clair");
        lightTheme.setToggleGroup(themeGroup);
        lightTheme.setSelected(true);
        
        RadioButton darkTheme = new RadioButton("Sombre");
        darkTheme.setToggleGroup(themeGroup);
        
        HBox themeBox = new HBox(20, lightTheme, darkTheme);
        
        // Option de notifications
        Label notifLabel = new Label("Notifications :");
        notifLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        CheckBox soundNotif = new CheckBox("Son à la réception");
        soundNotif.setSelected(true);
        
        CheckBox desktopNotif = new CheckBox("Notifications bureau");
        desktopNotif.setSelected(true);
        
        content.getChildren().addAll(
            themeLabel, themeBox,
            new Separator(),
            notifLabel, soundNotif, desktopNotif
        );
        
        dialog.getDialogPane().setContent(content);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            String selectedTheme = lightTheme.isSelected() ? "clair" : "sombre";
            applyTheme(selectedTheme);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Paramètres");
            alert.setHeaderText(null);
            alert.setContentText("Paramètres enregistrés !");
            alert.showAndWait();
        }
    }
    
    // ========== NOUVELLE MÉTHODE : Appliquer le thème ==========
    private void applyTheme(String theme) {
        if (theme.equals("sombre")) {
            this.setStyle("-fx-background-color: #1e1e1e;");
            // À compléter plus tard
        } else {
            this.setStyle("-fx-background-color: #efeae2;");
        }
    }
    
    private void selectConversation(ConversationItem conversation) {
        this.currentConversation = conversation;
        
        boolean isPrivate = conversation.getType() == ConversationType.PRIVATE;
        messageListView.getProperties().put("conversationType", isPrivate);
        
        conversationTitleLabel.setText(conversation.getName());
        
        if (conversation.getType() == ConversationType.PUBLIC_GROUP) {
            conversationStatusLabel.setText("Groupe public");
        } else if (conversation.getOtherUser() != null) {
            conversationStatusLabel.setText("Conversation privée");
        }
        
        messages.clear();
        
        if (conversation.getType() == ConversationType.PUBLIC_GROUP) {
            MessageText welcomeMsg = new MessageText("Bienvenue dans le groupe " + conversation.getName(), 
                new User("Système"));
            welcomeMsg.setVisibility("public");
            messages.add(welcomeMsg);
        }
        
        messageListView.refresh();
    }
    
    private void handleUserSelected(User selectedUser) {
        String conversationId = "private_" + selectedUser.getName();
        
        ConversationItem privateConv = findPrivateConversation(selectedUser);
        
        if (privateConv == null) {
            privateConv = new ConversationItem(
                conversationId,
                selectedUser.getName(),
                ConversationType.PRIVATE
            );
            privateConv.setOtherUser(selectedUser);
            addConversation(privateConv);
        }
        
        conversationListView.getSelectionModel().select(privateConv);
        selectConversation(privateConv);
    }
    
    private ConversationItem findPrivateConversation(User user) {
        for (ConversationItem conv : conversations) {
            if (conv.getType() == ConversationType.PRIVATE && 
                conv.getOtherUser() != null && 
                conv.getOtherUser().getName().equals(user.getName())) {
                return conv;
            }
        }
        return null;
    }
    
    // ========== MÉTHODES PUBLIQUES ==========
    
    public MessageText getMessageText() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return null;
        
        User sender = new User("Moi");
        MessageText message = new MessageText(text, sender);
        
        if (currentConversation != null) {
            if (currentConversation.getType() == ConversationType.PRIVATE) {
                message.setVisibility("private");
            } else {
                message.setVisibility("public");
            }
        }
        
        return message;
    }
    
    public void clearInput() {
        inputField.clear();
    }
    
    public void addMessage(Message msg) {
        messages.add(msg);
        messageListView.scrollTo(messages.size() - 1);
        
        if (currentConversation != null) {
            String lastMessage = msg instanceof MessageText ? 
                ((MessageText) msg).getContent() : "📎 Fichier";
            updateConversationLastMessage(currentConversation.getId(), lastMessage);
        }
    }
    
    public void setOnSendAction(EventHandler<ActionEvent> handler) {
        sendButton.setOnAction(handler);
        inputField.setOnAction(handler);
    }
    
    public void setOnFileSelected(EventHandler<ActionEvent> handler) {
        fileButton.setOnAction(handler);
    }
    
    public void setOnQuitAction(EventHandler<ActionEvent> handler) {
        quitButton.setOnAction(handler);
    }
    
    public File chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier à envoyer");
        
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Documents texte", "*.txt", "*.doc", "*.docx")
        );
        
        return fileChooser.showOpenDialog(this.getScene().getWindow());
    }
    
    public MessageFile createFileMessage(File file, User sender) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            MessageFile fileMessage = new MessageFile(file.getName(), fileData);
            fileMessage.setSender(sender);
            fileMessage.setMine(true);
            
            if (currentConversation != null) {
                if (currentConversation.getType() == ConversationType.PRIVATE) {
                    fileMessage.setVisibility("private");
                } else {
                    fileMessage.setVisibility("public");
                }
            }
            
            return fileMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public ConversationItem getSelectedConversation() {
        return conversationListView.getSelectionModel().getSelectedItem();
    }
    
    public void addConversation(ConversationItem conversation) {
        conversations.add(conversation);
    }
    
    public void updateConversationLastMessage(String conversationId, String lastMessage) {
        for (ConversationItem conv : conversations) {
            if (conv.getId().equals(conversationId)) {
                conv.setLastMessage(lastMessage);
                conv.setLastMessageTime(LocalTime.now());
                if (conv != currentConversation) {
                    conv.incrementUnreadCount();
                }
                conversationListView.refresh();
                break;
            }
        }
    }
    
    public void setOnEmojiAction(EventHandler<ActionEvent> handler) {
        emojiButton.setOnAction(handler);
    }
    
    public UserListView getUserListView() {
        return userListView;
    }
    
    // ========== NOUVELLES MÉTHODES PUBLIQUES POUR LA BARRE DE STATUT ==========
    public void setConnectionStatus(boolean connected, int count) {
        if (connected) {
            connectionStatusLabel.setText("● Connecté");
            connectionStatusLabel.setStyle("-fx-text-fill: #00a884; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            connectionStatusLabel.setText("● Déconnecté");
            connectionStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
        onlineCountLabel.setText("👥 " + count + " en ligne");
    }
    
    public void updateOnlineCount(int count) {
        onlineCountLabel.setText("👥 " + count + " en ligne");
    }
}