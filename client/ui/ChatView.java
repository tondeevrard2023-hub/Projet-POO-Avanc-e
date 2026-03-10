package ui;

import java.time.LocalDateTime;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import managerui.Theme;
import model.Message;
import model.MessageFile;
import model.MessageText;
import model.User;
import model.ConversationItem;

public class ChatView extends BorderPane {

    // Liste des conversations
    private ListView<ConversationItem> conversationListView;
    private ObservableList<ConversationItem> conversations;
    
    // Zone de chat principale
    private ListView<Message> messageListView;
    private ObservableList<Message> messages;
    private Map<String, ObservableList<Message>> conversationMessages = new HashMap<>();
    
    // Zone de saisie
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
    
    public ChatView() {
        conversations = FXCollections.observableArrayList();
        messages = FXCollections.observableArrayList();
        
        VBox leftPanel = createLeftPanel();
        BorderPane centerPanel = createCenterPanel();
        
        HBox mainLayout = new HBox(0);
        mainLayout.getChildren().addAll(leftPanel, centerPanel);
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        
        // ========== Ajout de la barre de statut ==========
        VBox rootLayout = new VBox();
        rootLayout.getChildren().addAll(mainLayout, createStatusBar());
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
        
        this.setCenter(rootLayout);
    }
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(350);
        leftPanel.getStyleClass().add("left-panel");
        
        HBox headerBox = new HBox(10);
        headerBox.setPadding(new Insets(15, 10, 15, 10));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("left-header");
        
        Label appTitle = new Label("Discussions");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Button newChatButton = new Button("➕");
        newChatButton.getStyleClass().add("icon-button");
        newChatButton.setTooltip(new Tooltip("Nouvelle discussion"));
        
        Button menuButton = new Button("☰");
        menuButton.getStyleClass().add("icon-button");
        menuButton.setTooltip(new Tooltip("Menu"));
        
        // ========== MODIFICATION : Ajout du bouton paramètres ==========
        settingsButton = new Button("⚙️");
        settingsButton.getStyleClass().add("icon-button");
        settingsButton.setTooltip(new Tooltip("Paramètres"));
        settingsButton.setOnAction(e -> showSettingsDialog());
        
        HBox rightButtons = new HBox(10, newChatButton, menuButton, settingsButton);
        rightButtons.setAlignment(Pos.CENTER_RIGHT);
        
        headerBox.getChildren().addAll(appTitle, rightButtons);
        HBox.setHgrow(rightButtons, Priority.ALWAYS);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher une discussion...");
        searchField.getStyleClass().add("search-field");
        
        HBox searchBox = new HBox(searchField);
        searchBox.setPadding(new Insets(10, 10, 5, 10));
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");
        
        Tab allTab = new Tab("Toutes");
        Tab groupsTab = new Tab("Groupes");
        Tab privateTab = new Tab("Privées");
        
        tabPane.getTabs().addAll(allTab, groupsTab, privateTab);
        
        conversationListView = new ListView<>(conversations);
        conversationListView.setCellFactory(list -> new ConversationCell());
        conversationListView.getStyleClass().add("conversation-list");
        
        conversationListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectConversation(newVal);
                }
            }
        );
        
        Label contactsSeparator = new Label();
        contactsSeparator.setFont(Font.font("System", FontWeight.BOLD, 14));
        contactsSeparator.setPadding(new Insets(15, 10, 5, 10));
        contactsSeparator.getStyleClass().add("contacts-separator");
        
        userListView = new UserListView();
        userListView.setPrefHeight(200);
        userListView.setOnUserSelected(this::handleUserSelected);
        
        VBox.setVgrow(conversationListView, Priority.NEVER);
        
        leftPanel.getChildren().addAll(
            headerBox, 
            searchBox, 
            tabPane,
            conversationListView,
            contactsSeparator,
            userListView
        );
        
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        return leftPanel;
    }
    
    private BorderPane createCenterPanel() {
        BorderPane centerPanel = new BorderPane();
        centerPanel.getStyleClass().add("center-panel");
        
        HBox chatHeader = createChatHeader();
        centerPanel.setTop(chatHeader);
        
        messageListView = new ListView<>(messages);
        messageListView.setCellFactory(list -> new MessageCell());
        messageListView.getStyleClass().add("message-list");
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
        header.getStyleClass().add("chat-header");        
        
        conversationTitleLabel = new Label("Sélectionnez une conversation");
        conversationTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        conversationStatusLabel = new Label("");
        conversationStatusLabel.setFont(Font.font(12));
        conversationStatusLabel.getStyleClass().add("conversation-status");
        
        VBox titleBox = new VBox(2);
        titleBox.getChildren().addAll(conversationTitleLabel, conversationStatusLabel);
        
        infoButton = new Button("ℹ️");
        infoButton.getStyleClass().add("info-button");
        infoButton.setTooltip(new Tooltip("Informations de la conversation"));
        
        HBox rightBox = new HBox(infoButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        header.getChildren().addAll(titleBox, rightBox);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        
        return header;
    }
    
    private VBox createInputArea() {
        VBox inputArea = new VBox();
        inputArea.getStyleClass().add("input-area");
        
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10, 15, 10, 15));
        inputBox.setAlignment(Pos.CENTER_LEFT);
        
        emojiButton = new Button("😊");
        emojiButton.getStyleClass().add("emoji-button");
        emojiButton.setTooltip(new Tooltip("Ajouter un emoji"));
        
        inputField = new TextField();
        inputField.setPromptText("Écrivez un message...");
        inputField.getStyleClass().add("message-input");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        
        fileButton = new Button("📎");
        fileButton.getStyleClass().add("file-button");
        fileButton.setTooltip(new Tooltip("Joindre un fichier"));
        
        sendButton = new Button("➤");
        sendButton.getStyleClass().add("send-button");
        sendButton.setTooltip(new Tooltip("Envoyer"));
        
        quitButton = new Button("✕");
        quitButton.getStyleClass().add("quit-button");
        quitButton.setTooltip(new Tooltip("Quitter le chat"));
        
        inputBox.getChildren().addAll(emojiButton, inputField, fileButton, sendButton, quitButton);
        
        inputArea.getChildren().add(inputBox);
        
        return inputArea;
    }
    
    // ========== Barre de statut ==========
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 15, 5, 15));
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        connectionStatusLabel = new Label("● Connecté");
        
        onlineCountLabel = new Label("👥 0 en ligne");
        onlineCountLabel.getStyleClass().add("online-count");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label versionLabel = new Label("v5.0.0");
        versionLabel.getStyleClass().add("version-label");
        
        statusBar.getChildren().addAll(connectionStatusLabel, onlineCountLabel, spacer, versionLabel);
        
        return statusBar;
    }
    
    // ========== Dialogue des paramètres ==========
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
        lightTheme.setSelected(Theme.getCurrentTheme().equals("light"));
        
        RadioButton darkTheme = new RadioButton("Sombre");
        darkTheme.setToggleGroup(themeGroup);
        darkTheme.setSelected(Theme.getCurrentTheme().equals("dark"));

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
            String selectedTheme = lightTheme.isSelected() ? "light" : "dark";
            Theme.apply(selectedTheme);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Paramètres");
            alert.setHeaderText(null);
            alert.setContentText("Paramètres enregistrés !");
            alert.showAndWait();
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
        
        // Charger les messages de cette conversation
        ObservableList<Message> convMessages = conversationMessages.get(conversation.getId());
        if (convMessages == null) {
            convMessages = FXCollections.observableArrayList();
            conversationMessages.put(conversation.getId(), convMessages);
        }
        messages = convMessages;
        messageListView.setItems(messages);
        
        // Réinitialiser le compteur de messages non lus
        conversation.setUnreadCount(0);
        conversationListView.refresh();
    }
    
    // méthode pour ajouter un message à une conversation spécifique
    public void addMessageToConversation(String conversationId, Message message) {
        ObservableList<Message> convMessages = conversationMessages.get(conversationId);

        if (convMessages == null) {
            convMessages = FXCollections.observableArrayList();
            conversationMessages.put(conversationId, convMessages);
        }
        convMessages.add(message);
        
        // Mettre à jour le dernier message dans la liste des conversations
        String lastMessageText = (message instanceof MessageText)
            ? ((MessageText) message).getContent()
            : "📎 Fichier";
        updateConversationLastMessage(conversationId, lastMessageText);
        
        // Si c'est la conversation courante, s'assurer que la ListView est mise à jour
        if (currentConversation != null && currentConversation.getId().equals(conversationId)) {
            messageListView.scrollTo(convMessages.size() - 1);
        } else {
            // Sinon, incrémenter le compteur de messages non lus
            for (ConversationItem conv : conversations) {
                if (conv.getId().equals(conversationId)) {
                    conv.incrementUnreadCount();
                    break;
                }
            }
        }
    }

    private void handleUserSelected(User selectedUser) {
        String conversationId = "private_" + selectedUser.getName();
        
        ConversationItem privateConv = findPrivateConversation(selectedUser);
        
        if (privateConv == null) {
            privateConv = new ConversationItem(conversationId, selectedUser.getName(), ConversationType.PRIVATE);
            privateConv.setOtherUser(selectedUser);
            conversations.add(privateConv);
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
        
        MessageText message = new MessageText(text);
        
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
        /* if (currentConversation != null) {
            addMessageToConversation(currentConversation.getId(), msg);
        } else {
            System.out.println("Message reçu mais aucune conversation active");
        } */
       // 
       addMessageToConversation(currentConversation.getId(), msg);
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
                conv.setLastMessageTime(LocalDateTime.now());
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
    
    public void setConnectionStatus(boolean connected, int count) {
        connectionStatusLabel.getStyleClass().removeAll("connection-connected", "connection-disconnected");
        if (connected) {
            connectionStatusLabel.setText("● Connecté");
            connectionStatusLabel.getStyleClass().add("connection-connected");
        } else {
            connectionStatusLabel.setText("● Déconnecté");
            connectionStatusLabel.getStyleClass().add("connection-disconnected");
        }
        onlineCountLabel.setText("👥 " + count + " en ligne");
    }
    
    public void updateOnlineCount(int count) {
        onlineCountLabel.setText("👥 " + count + " en ligne");
    }

    public ConversationItem getOrCreatePrivateConversation(String userId, User user) {
        for (ConversationItem conv : conversations) {
            if (conv.getId().equals(userId)) {
                return conv;
            }
        }
        ConversationItem newConv = new ConversationItem(userId, user.getName(), ConversationType.PRIVATE);
        newConv.setOtherUser(new User(user.getName()));
        conversations.add(newConv);
        return newConv;
    }

    public void ensurePublicGroupExists() {
        String publicId = "public_group";
        for (ConversationItem conv : conversations) {
            if (conv.getId().equals(publicId)) return;
        }
        ConversationItem publicGroup = new ConversationItem(publicId, "Général", ConversationType.PUBLIC_GROUP);
        conversations.add(publicGroup);
    }

    public ConversationItem getCurrentConversation() {
        return currentConversation;
    }

    public ListView<ConversationItem> getConversationListView() {
        return conversationListView;
    }
}