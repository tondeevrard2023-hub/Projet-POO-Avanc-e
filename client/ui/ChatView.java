package ui;

import java.time.LocalTime;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Message;
import model.MessageText;
import model.User;

// AJOUT : Importer UserListView
import ui.UserListView;

public class ChatView extends BorderPane {

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
    
    // En-tête de conversation
    private Label conversationTitleLabel;
    private Label conversationStatusLabel;
    private Button infoButton;
    
    // Conversation actuelle
    private ConversationItem currentConversation;
    
    // AJOUT : Déclarer UserListView
    private UserListView userListView;
    
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
        private User otherUser; // Pour les conversations privées
        
        public ConversationItem(String id, String name, ConversationType type) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.lastMessage = "";
            this.lastMessageTime = LocalTime.now();
            this.unreadCount = 0;
        }
        
        // Getters et setters
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
                
                // Avatar ou icône selon le type
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
                
                // Informations de conversation
                VBox infoBox = new VBox(3);
                
                Label nameLabel = new Label(item.getName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                
                Label lastMessageLabel = new Label(item.getLastMessage());
                lastMessageLabel.setFont(Font.font(12));
                lastMessageLabel.setStyle("-fx-text-fill: #667781;");
                lastMessageLabel.setMaxWidth(150);
                
                infoBox.getChildren().addAll(nameLabel, lastMessageLabel);
                
                // Heure et compteur de messages non lus
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
                
                // Style de fond pour la conversation sélectionnée
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

    public ChatView() {
        // Initialisation des collections
        conversations = FXCollections.observableArrayList();
        messages = FXCollections.observableArrayList();
        
        // Panneau latéral gauche (liste des conversations + utilisateurs)
        VBox leftPanel = createLeftPanel();
        
        // Panneau central (zone de chat)
        BorderPane centerPanel = createCenterPanel();
        
        // Assemblage principal
        HBox mainLayout = new HBox(0);
        mainLayout.getChildren().addAll(leftPanel, centerPanel);
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        
        this.setCenter(mainLayout);
        
        // Ajouter quelques conversations de démo
       // addDemoConversations();
    }
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(350);
        leftPanel.setStyle("-fx-background-color: white; -fx-border-color: #e9edef; -fx-border-width: 0 1 0 0;");
        
        // En-tête du panneau gauche
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
        
        HBox rightButtons = new HBox(10, newChatButton, menuButton);
        rightButtons.setAlignment(Pos.CENTER_RIGHT);
        
        headerBox.getChildren().addAll(appTitle, rightButtons);
        HBox.setHgrow(rightButtons, Priority.ALWAYS);
        
        // Barre de recherche
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher une discussion...");
        searchField.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 20; -fx-padding: 8 15;");
        
        HBox searchBox = new HBox(searchField);
        searchBox.setPadding(new Insets(10, 10, 5, 10));
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        // Tabs pour filtrer les conversations
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white; -fx-tab-min-width: 120;");
        
        Tab allTab = new Tab("Toutes");
        Tab groupsTab = new Tab("Groupes");
        Tab privateTab = new Tab("Privées");
        
        tabPane.getTabs().addAll(allTab, groupsTab, privateTab);
        
        // Liste des conversations
        conversationListView = new ListView<>(conversations);
        conversationListView.setCellFactory(list -> new ConversationCell());
        conversationListView.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
        
        // Gestionnaire de sélection de conversation
        conversationListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectConversation(newVal);
                }
            }
        );
        
        // AJOUT : Créer et ajouter UserListView
        // Séparateur pour les contacts
        Label contactsSeparator = new Label("Contacts");
        contactsSeparator.setFont(Font.font("System", FontWeight.BOLD, 14));
        contactsSeparator.setPadding(new Insets(15, 10, 5, 10));
        contactsSeparator.setStyle("-fx-text-fill: #00a884;");
        
        // Créer la liste des utilisateurs
        userListView = new UserListView();
        userListView.setPrefHeight(200);
        userListView.setStyle("-fx-background-color: white;");
        
        // Gestionnaire de sélection d'utilisateur
        userListView.setOnUserSelected(this::handleUserSelected);
        
        VBox.setVgrow(conversationListView, Priority.ALWAYS);
        
        // AJOUT : Ajouter tous les éléments au panneau gauche
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
        
        // En-tête de conversation
        HBox chatHeader = createChatHeader();
        centerPanel.setTop(chatHeader);
        
        // Zone des messages
        messageListView = new ListView<>(messages);
        messageListView.setCellFactory(list -> new MessageCell());
        messageListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        messageListView.setPadding(new Insets(10));
        
        centerPanel.setCenter(messageListView);
        
        // Zone de saisie
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
        
        // Zone de saisie de message
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10, 15, 10, 15));
        inputBox.setAlignment(Pos.CENTER_LEFT);
        
        emojiButton = new Button("😊");
        emojiButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20;");
        emojiButton.setTooltip(new Tooltip("Ajouter un emoji"));
        
        inputField = new TextField();
        inputField.setPromptText("Écrivez un message...");
        inputField.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 8 15;");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        
        fileButton = new Button("📎");
        fileButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20;");
        fileButton.setTooltip(new Tooltip("Joindre un fichier"));
        
        sendButton = new Button("📤");
        sendButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20;");
        sendButton.setTooltip(new Tooltip("Envoyer"));
        
        inputBox.getChildren().addAll(emojiButton, inputField, fileButton, sendButton);
        
        inputArea.getChildren().add(inputBox);
        
        return inputArea;
    }
    
    private void selectConversation(ConversationItem conversation) {
        this.currentConversation = conversation;
        
        // AJOUT : Mettre à jour le type de conversation dans MessageCell
        boolean isPrivate = conversation.getType() == ConversationType.PRIVATE;
        messageListView.getProperties().put("conversationType", isPrivate);
        
        // Mettre à jour l'en-tête
        conversationTitleLabel.setText(conversation.getName());
        
        if (conversation.getType() == ConversationType.PUBLIC_GROUP) {
            conversationStatusLabel.setText("Groupe public");
        } else if (conversation.getOtherUser() != null) {
            conversationStatusLabel.setText("Conversation privée");
        }
        
        // Charger les messages de cette conversation (à implémenter avec le backend)
        messages.clear();
        
        // Ajouter un message de bienvenue
        if (conversation.getType() == ConversationType.PUBLIC_GROUP) {
            MessageText welcomeMsg = new MessageText("Bienvenue dans le groupe " + conversation.getName(), 
                new User("Système"));
            welcomeMsg.setVisibility("public");
            messages.add(welcomeMsg);
        }
        
        // Forcer le rafraîchissement des cellules
        messageListView.refresh();
    }
    
    // AJOUT : Gérer la sélection d'un utilisateur
    private void handleUserSelected(User selectedUser) {
        // Créer ou ouvrir une conversation privée avec cet utilisateur
        String conversationId = "private_" + selectedUser.getName();
        
        // Vérifier si la conversation existe déjà
        ConversationItem privateConv = findPrivateConversation(selectedUser);
        
        if (privateConv == null) {
            // Créer une nouvelle conversation privée
            privateConv = new ConversationItem(
                conversationId,
                selectedUser.getName(),
                ConversationType.PRIVATE
            );
            privateConv.setOtherUser(selectedUser);
            addConversation(privateConv);
        }
        
        // Sélectionner cette conversation
        conversationListView.getSelectionModel().select(privateConv);
        selectConversation(privateConv);
    }
    
    // AJOUT : Trouver une conversation privée
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
    
    private void addDemoConversations() {
        // Conversations de groupe (publiques)
        ConversationItem group1 = new ConversationItem("g1", "Général", ConversationType.PUBLIC_GROUP);
        group1.setLastMessage("Salut tout le monde !");
        group1.setLastMessageTime(LocalTime.now().minusHours(2));
        
        ConversationItem group2 = new ConversationItem("g2", "Projet Java", ConversationType.PUBLIC_GROUP);
        group2.setLastMessage("Quand est la prochaine réunion ?");
        group2.setLastMessageTime(LocalTime.now().minusMinutes(30));
        
        // Conversations privées
        User user1 = new User("Alice");
        ConversationItem private1 = new ConversationItem("p1", "Alice", ConversationType.PRIVATE);
        private1.setOtherUser(user1);
        private1.setLastMessage("Tu viens ce soir ?");
        private1.setLastMessageTime(LocalTime.now().minusMinutes(15));
        private1.setUnreadCount(2);
        
        User user2 = new User("Bob");
        ConversationItem private2 = new ConversationItem("p2", "Bob", ConversationType.PRIVATE);
        private2.setOtherUser(user2);
        private2.setLastMessage("OK, à demain !");
        private2.setLastMessageTime(LocalTime.now().minusHours(5));
        
        conversations.addAll(group1, group2, private1, private2);
    }
    
    // Méthodes publiques pour interagir avec la vue
    
    public MessageText getMessageText() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return null;
        
        // Utiliser l'utilisateur approprié selon la conversation
        User sender = new User("Moi"); // À remplacer par l'utilisateur connecté
        MessageText message = new MessageText(text, sender);
        
        // AJOUT : Définir la visibilité en fonction de la conversation
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
        
        // AJOUT : Mettre à jour le dernier message dans la liste des conversations
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
    
    public void setOnFileAction(EventHandler<ActionEvent> handler) {
        fileButton.setOnAction(handler);
    }
    
    public void setOnEmojiAction(EventHandler<ActionEvent> handler) {
        emojiButton.setOnAction(handler);
    }
    
    public void setOnNewChatAction(EventHandler<ActionEvent> handler) {
        // À implémenter avec le bouton "Nouvelle discussion"
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
    
    // AJOUT : Getter pour UserListView (optionnel)
    public UserListView getUserListView() {
        return userListView;
    }
}