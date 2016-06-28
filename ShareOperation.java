// package name and imports was removed in order of NDA
public abstract class ShareOperation {

    /**
     * Callback interface to observer sharing operation progress.
     */
    /* package */ interface Callback {

        /**
         * Continue share operation when result is true.
         */
        boolean onProgress(@IntRange(from = 0, to = 100) final int progress);

    }

    @NonNull
    private final Source mSource;

    @NonNull
    private final SendTo mSendTo;

    public ShareOperation(@NonNull final Source source, @NonNull final SendTo sendTo) {
        mSource = source;
        mSendTo = sendTo;
    }

    @WorkerThread
    public abstract void doInBackground(@NonNull final Context context,
                                        @NonNull final Callback callback)
            throws Exception;

    @MainThread
    public void onPostExecute(@NonNull final Context context, @NonNull final Launchable launchable) {
    }

    @MainThread
    public void onFailed(@NonNull final Context context) {
    }

    @NonNull
    public SendTo getSendTo() {
        return mSendTo;
    }

    /**
     * Return {@link Document} to be shared in this operation.
     */
    @NonNull
    public Document getDocument(@NonNull final Context context) {
        final long documentId = mSource.getDocumentId();

        final ContentResolver contentResolver = context.getContentResolver();
        final Document document = DocumentsContract.getDocument(contentResolver, documentId);
        if (document == null) {
            throw new IllegalArgumentException("There are no document in the database to be shared.");
        }
        return document;
    }

    /**
     * Return list of {@link Uri}s to be shared in this operation.
     */
    @NonNull
    public List<Page> getPages(@NonNull final Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        if (hasUserSelectedPages()) {
            final LongArrayList sourcePages = mSource.getPages();
            final int count = sourcePages.size();
            if (count == 1) {
                final long pageId = sourcePages.get(0);
                final Page page = PagesContract.getPage(contentResolver, pageId);
                return Collections.singletonList(page);

            } else {
                // Select pages from all document pages to keep order right
                final long documentId = mSource.getDocumentId();
                final List<Page> documentPages = PagesContract
                        .getDocumentPages(contentResolver, documentId);

                final List<Page> pages = new ArrayList<>();
                for (final Page page : documentPages) {
                    if (sourcePages.contains(page.getId())) {
                        pages.add(page);
                    }
                }

                return pages;
            }

        } else {
            // There are no user-selected pages, so load all pages from document
            final long documentId = mSource.getDocumentId();
            return PagesContract.getDocumentPages(contentResolver, documentId);
        }
    }

    /**
     * Return true if source has user selected pages, false otherwise.
     */
    public boolean hasUserSelectedPages() {
        return !mSource.getPages().isEmpty();
    }

}
