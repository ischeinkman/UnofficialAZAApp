package org.ramonaza.unofficialazaapp.helpers.backend;


import android.content.Context;
import android.os.Environment;

import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonazaapi.chapterpacks.ChapterPackHandler;
import org.ramonazaapi.contacts.ContactCSVHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Calendar;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by ilan on 9/18/15.
 */
public class ChapterPackHandlerSupport {

    public static final String NEW_PACK_DIRECTORY = "Generated Packs/";
    private static ChapterPackHandler currentHandler;
    private static ContactDatabaseHandler contactHandler;
    /**
     * Check if we currently have a Chapter Pack leaded into the app.
     *
     * @return if we have a loaded Chapter Pack
     */
    public static boolean chapterPackIsLoaded() {
        return currentHandler != null;
    }

    /**
     * Get all possible Chapter Pack files in the Download directory.
     *
     * @return an array containing all Chapter Pack files
     */
    public static File[] getOptions() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] ddFiles = downloadDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.contains(ChapterPackHandler.PREFIX)
                        && (dir.isDirectory()
                        || filename.contains(".zip")));

            }
        });
        return ddFiles;
    }

    /**
     * Gets the current Chapter Pack.
     * If no pack is or could be loaded, we return null.
     *
     * @param context the context to use
     * @return the currently loaded Chapter Pack, or null
     */
    public static Observable<ChapterPackHandler> getChapterPackHandler(Context context) {
        return Observable.just(currentHandler);
    }

    /**
     * Loads the current Chapter Pack from a given File object.
     *
     * @param context the context to use
     * @param pack    the file to load the pack from. Can either be a
     *                folder or zip file.
     * @return the current Chapter Pack
     */
    public static Observable<ChapterPackHandler> getChapterPackHandler(final Context context, final File pack) {

        //If the new pack is the same as the old pack, return the old pack.
        if (currentHandler != null && currentHandler.getPackName().equals(pack.getName()))
            return Observable.just(currentHandler);

        return Observable.just(currentHandler = new ChapterPackHandler(pack))
                .map(new Func1<ChapterPackHandler, ChapterPackHandler>() {
                    @Override
                    public ChapterPackHandler call(ChapterPackHandler chapterPackHandler) {
                        //Store the new event feed for later use
                        PreferenceHelper.getPreferences(context).putChapterPackName(pack.getName()).putEventFeed(currentHandler.getEventUrl());
                        return currentHandler;
                    }
                })
                .flatMap(new Func1<ChapterPackHandler, Observable<List<ContactInfoWrapper>>>() {
                    @Override
                    public Observable<List<ContactInfoWrapper>> call(ChapterPackHandler chapterPackHandler) {
                        final ContactDatabaseHandler ctdbh = new ContactDatabaseHandler(context);
                        return currentHandler.getCsvHandler().getCtactInfoListFromCSV()
                                .toList()
                                .flatMap(new Func1<List<ContactInfoWrapper>, Observable<List<ContactInfoWrapper>>>() {
                                    @Override
                                    public Observable<List<ContactInfoWrapper>> call(final List<ContactInfoWrapper> inPack) {
                                        if (inPack.size() > 0) {
                                            return ctdbh.deleteContacts(null, null)
                                                    .flatMap(new Func1<Integer, Observable<List<ContactInfoWrapper>>>() {
                                                        @Override
                                                        public Observable<List<ContactInfoWrapper>> call(Integer integer) {
                                                            return ctdbh.addContacts(inPack.toArray(new ContactInfoWrapper[0])).toList();
                                                        }
                                                    });
                                        }
                                        return Observable.just(inPack);
                                    }
                                });

                    }
                }).toList()
                .map(new Func1<List<List<ContactInfoWrapper>>, ChapterPackHandler>() {
                    @Override
                    public ChapterPackHandler call(List<List<ContactInfoWrapper>> lists) {
                        return currentHandler;
                    }
                });
    }

    /**
     * Gets the current ContactDatabaseHandler from the environment.
     * First checks if we have a newly loaded Chapter Pack, and if we do
     * return the pack's handler. If not we return a handler created from
     * the provided context.
     *
     * @param context the context to use
     * @return the currently loaded contact handler
     */
    public static ContactDatabaseHandler getContactHandler(Context context) {
        if (contactHandler == null) contactHandler = new ContactDatabaseHandler(context);
        return contactHandler;
    }

    /**
     * Loads all the data from the current Chapter Pack into the
     * app's storage.
     *
     * @param context the context to use
     * @return whether the loading was successful or not
     */
    public static boolean loadChapterPack(Context context) {
        if (currentHandler == null) getChapterPackHandler(context);
        return (currentHandler.loadEventFeed() && currentHandler.loadContactList());
    }

    public static Observable<ChapterPackHandler> createChapterPack(final Context context) {
        return getPackName(context)
                .map(new Func1<String, File>() {
                    @Override
                    public File call(String s) {
                        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File generalPackDir = new File(downloadDir, NEW_PACK_DIRECTORY);
                        return new File(generalPackDir, s);
                    }
                })
                .flatMap(new Func1<File, Observable<File>>() {
                    @Override
                    public Observable<File> call(File file) {
                        return createEventFile(context, file);
                    }
                })
                .flatMap(new Func1<File, Observable<File>>() {
                    @Override
                    public Observable<File> call(File file) {
                        return createContactFile(context, file);
                    }
                })
                .map(new Func1<File, ChapterPackHandler>() {
                    @Override
                    public ChapterPackHandler call(File file) {
                        return new ChapterPackHandler(file);
                    }
                });
    }

    private static Observable<String> getPackName(final Context context) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    Calendar cal = Calendar.getInstance();
                    String suffix = String.format(
                            " -- %d - %d - %d - %d:%d:%d",
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.HOUR),
                            cal.get(Calendar.MINUTE),
                            cal.get(Calendar.SECOND)
                    );
                    String base = PreferenceHelper.getPreferences(context).getChapterPackName();
                    if (base == null || base.equals("")) base = "Chapter Pack";
                    subscriber.onNext(base + suffix);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static Observable<File> createEventFile(final Context context, final File packdir) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                try {
                    String streamUrl = PreferenceHelper.getPreferences(context).getEventFeed();
                    File eventDataFile = new File(packdir, ChapterPackHandler.EVENT_FILE_NAME);
                    eventDataFile.createNewFile();
                    BufferedWriter eventWriter = new BufferedWriter(new FileWriter(eventDataFile));
                    eventWriter.write(streamUrl);
                    eventWriter.flush();
                    eventWriter.close();
                    subscriber.onNext(packdir);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static Observable<File> createContactFile(Context context, final File packdir) {
        ContactDatabaseHandler dbHandler = getContactHandler(context);
        return dbHandler.getContacts(null)
                .toList()
                .flatMap(new Func1<List<ContactInfoWrapper>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(List<ContactInfoWrapper> contactInfoWrappers) {
                        ContactCSVHandler csvHandler;
                        try {
                            File csvFile = new File(packdir, ChapterPackHandler.CSV_NAME);
                            csvFile.createNewFile();
                            csvHandler = new ContactCSVHandler(csvFile);
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                        return csvHandler.writesContactsToCSV(contactInfoWrappers, false);
                    }
                }).map(new Func1<Boolean, File>() {
                    @Override
                    public File call(Boolean aBoolean) {
                        return packdir;
                    }
                });
    }
}
