package hr.bmestric.sevens.ui.service;

import hr.bmestric.sevens.persistence.AsyncStorageService;

import java.util.Objects;

public class UiServices {
    private final FxDialogService dialogService;
    private final GameFileDialogService fileDialogService;
    private final AsyncStorageService storageService;
    private final ISessionFactory sessionFactory;

    public UiServices(FxDialogService dialogService,
                      GameFileDialogService fileDialogService,
                      AsyncStorageService storageService,
                      ISessionFactory sessionFactory) {
        this.dialogService = Objects.requireNonNull(dialogService, "dialogService");
        this.fileDialogService = Objects.requireNonNull(fileDialogService, "fileDialogService");
        this.storageService = Objects.requireNonNull(storageService, "storageService");
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "sessionFactory");
    }

    // Backwards-compatible constructor
    public UiServices(FxDialogService dialogService, GameFileDialogService fileDialogService, AsyncStorageService storageService) {
        this(dialogService, fileDialogService, storageService, new DefaultSessionFactory());
    }

    public FxDialogService dialogs() {
        return dialogService;
    }

    public GameFileDialogService fileDialogs() {
        return fileDialogService;
    }

    public AsyncStorageService storage() {
        return storageService;
    }

    public ISessionFactory sessions() {
        return sessionFactory;
    }
}
