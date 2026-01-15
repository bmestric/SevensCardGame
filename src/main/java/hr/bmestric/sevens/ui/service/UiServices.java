package hr.bmestric.sevens.ui.service;

import hr.bmestric.sevens.persistence.AsyncStorageService;

import java.util.Objects;

public class UiServices {
    private final FxDialogService dialogService;
    private final GameFileDialogService fileDialogService;
    private final AsyncStorageService storageService;
    private final ISessionFactory sessionFactory;
    private final AsyncRmiService asyncRmiService;
    private final UiDataPreparationService uiPrepService;

    public UiServices(FxDialogService dialogService,
                      GameFileDialogService fileDialogService,
                      AsyncStorageService storageService,
                      ISessionFactory sessionFactory,
                      AsyncRmiService asyncRmiService,
                      UiDataPreparationService uiPrepService) {
        this.dialogService = Objects.requireNonNull(dialogService, "dialogService");
        this.fileDialogService = Objects.requireNonNull(fileDialogService, "fileDialogService");
        this.storageService = Objects.requireNonNull(storageService, "storageService");
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "sessionFactory");
        this.asyncRmiService = Objects.requireNonNull(asyncRmiService, "asyncRmiService");
        this.uiPrepService = Objects.requireNonNull(uiPrepService, "uiPrepService");
    }

    // Backwards-compatible constructor
    public UiServices(FxDialogService dialogService, GameFileDialogService fileDialogService, AsyncStorageService storageService) {
        this(dialogService, fileDialogService, storageService, new DefaultSessionFactory(),
             new AsyncRmiService(), new UiDataPreparationService());
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

    public AsyncRmiService asyncRmi() {
        return asyncRmiService;
    }

    public UiDataPreparationService uiPrep() {
        return uiPrepService;
    }
}
