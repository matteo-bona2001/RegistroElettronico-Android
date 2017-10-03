package com.sharpdroid.registroelettronico.Utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Toast;

import com.orm.SugarRecord;
import com.sharpdroid.registroelettronico.API.V1.SpiaggiariAPI;
import com.sharpdroid.registroelettronico.API.V2.APIClient;
import com.sharpdroid.registroelettronico.Databases.Entities.Communication;
import com.sharpdroid.registroelettronico.Databases.Entities.Folder;
import com.sharpdroid.registroelettronico.Databases.Entities.Grade;
import com.sharpdroid.registroelettronico.Databases.Entities.Period;
import com.sharpdroid.registroelettronico.Databases.Entities.Profile;
import com.sharpdroid.registroelettronico.Databases.Entities.SubjectTeacher;
import com.sharpdroid.registroelettronico.Databases.Entities.SuperAgenda;
import com.sharpdroid.registroelettronico.Databases.Entities.Teacher;
import com.sharpdroid.registroelettronico.Databases.RegistroDB;
import com.sharpdroid.registroelettronico.Interfaces.API.Absence;
import com.sharpdroid.registroelettronico.Interfaces.API.Event;
import com.sharpdroid.registroelettronico.Interfaces.API.Lesson;
import com.sharpdroid.registroelettronico.Interfaces.API.Mark;
import com.sharpdroid.registroelettronico.Interfaces.API.MarkSubject;
import com.sharpdroid.registroelettronico.Interfaces.Client.AbsencesEntry;
import com.sharpdroid.registroelettronico.Interfaces.Client.Entry;
import com.sharpdroid.registroelettronico.Interfaces.Client.Media;
import com.sharpdroid.registroelettronico.Interfaces.Client.Subject;
import com.sharpdroid.registroelettronico.NotificationManager;
import com.sharpdroid.registroelettronico.R;

import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class Metodi {
    public static SimpleDateFormat month_year = new SimpleDateFormat("MMMM yyyy", Locale.ITALIAN);
    public static SimpleDateFormat complex = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.ITALIAN);
    public static char[] Delimeters = {'.', ' ', '\'', '/', '\\'};
    private static Looper mainLooper = Looper.getMainLooper();
    private static Handler handler = new Handler(mainLooper);

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static String MessaggioVoto(float Obb, float media, int nVoti) {
        // Calcolo
        if (Obb > 10 || media > 10)
            return "Errore"; // Quando l'obiettivo o la media sono > 10
        if (Obb >= 10 && media < Obb)
            return "Obiettivo irraggiungibile"; // Quando l'obiettivo è 10 (o più) e la media è < 10 (non si potrà mai raggiungere)
        double[] array = {0.75, 0.5, 0.25, 0};
        int index = 0;
        float sommaVotiDaPrendere;
        double[] votiMinimi = new double[5];
        double diff;
        double diff2;
        double resto = 0;
        double parteIntera;
        double parteDecimale;
        try {
            do {
                index = index + 1;
                sommaVotiDaPrendere = (Obb * (nVoti + index)) - (media * nVoti);
            } while ((sommaVotiDaPrendere / index) > 10);
            for (int i = 0; i < index; i = i + 1) {
                votiMinimi[i] = (sommaVotiDaPrendere / index) + resto;
                resto = 0;
                parteIntera = Math.floor(votiMinimi[i]);
                parteDecimale = (votiMinimi[i] - parteIntera) * 100;
                if (parteDecimale != 25 && parteDecimale != 50 && parteDecimale != 75) {
                    int k = 0;
                    do {
                        diff = votiMinimi[i] - (parteIntera + array[k]);
                        k++;
                    } while (diff < 0);
                    votiMinimi[i] = votiMinimi[i] - diff;
                    resto = diff;
                }
                if (votiMinimi[i] > 10) {
                    diff2 = votiMinimi[i] - 10;
                    votiMinimi[i] = 10;
                    resto = resto + diff2;
                }
            }
            // Stampa
            String toReturn;
            if (votiMinimi[0] <= 0)
                return "Puoi stare tranquillo"; // Quando i voti da prendere sono negativi
            if (votiMinimi[0] <= Obb)
                toReturn = "Non prendere meno di " + votiMinimi[0];
            else {
                toReturn = "Devi prendere almeno ";
                for (double aVotiMinimi : votiMinimi) {
                    if (aVotiMinimi != 0) {
                        toReturn = toReturn + aVotiMinimi + ", ";
                    }
                }
                toReturn = toReturn.substring(0, toReturn.length() - 2);
            }
            return toReturn;
        } catch (Exception e) {
            return "Obiettivo irraggiungibile";
        }
    }

    public static MyLinkedMap<String, Integer> sortByComparator(MyLinkedMap<String, Integer> unsortMap, final boolean order) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values

        Collections.sort(list, (Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> {
            if (order) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        MyLinkedMap<String, Integer> sortedMap = new MyLinkedMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static int getMarkColor(float voto, float voto_obiettivo) {
        if (voto >= voto_obiettivo)
            return R.color.greenmaterial;
        else if (voto < 5)
            return R.color.redmaterial;
        else if (voto >= 5 && voto < 6)
            return R.color.orangematerial;
        else
            return R.color.lightgreenmaterial;
    }

    public static int getMarkColor(Mark mark, float voto_obiettivo) {
        if (!mark.isNs()) {
            float voto = Float.parseFloat(mark.getMark());
            if (voto >= voto_obiettivo)
                return R.color.greenmaterial;
            else if (voto < 5)
                return R.color.redmaterial;
            else if (voto >= 5 && voto < 6)
                return R.color.orangematerial;
            else
                return R.color.lightgreenmaterial;
        } else {
            return R.color.intro_blue;
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int CalculateScholasticCredits(int year, double average) {
        switch (year) {
            case 3:
            case 4:
                if (average == 6) return 3;
                else if (average > 6 && average <= 7) return 4;
                else if (average > 7 && average <= 8) return 5;
                else if (average > 8 && average <= 9) return 6;
                else if (average > 9 && average <= 10) return 7;
                break;

            case 5:
                if (average == 6) return 4;
                else if (average > 6 && average <= 7) return 5;
                else if (average > 7 && average <= 8) return 6;
                else if (average > 8 && average <= 9) return 7;
                else if (average > 9 && average <= 10) return 8;
                break;

            default:
                return 0;
        }

        return 0;
    }

    public static int getMediaColor(Media media, String tipo, float voto_obiettivo) {
        switch (tipo) {
            case SpiaggiariAPI.ORALE:
                return getMarkColor(media.getMediaOrale(), voto_obiettivo);
            case SpiaggiariAPI.PRATICO:
                return getMarkColor(media.getMediaPratico(), voto_obiettivo);
            case SpiaggiariAPI.SCRITTO:
                return getMarkColor(media.getMediaScritto(), voto_obiettivo);
            default:
                return getMarkColor(media.getMediaGenerale(), voto_obiettivo);
        }
    }

    public static int getMediaColor(Media media, float voto_obiettivo) {
        return getMediaColor(media, "Generale", voto_obiettivo);
    }

    public static int getMediaColor(Float media, float voto_obiettivo) {
        return getMarkColor(media, voto_obiettivo);
    }

    public static int getNumberDaysAbsences(List<Absence> absences) {
        int days = 0;
        for (Absence a : absences) {
            days += a.getDays();
        }
        return days;
    }

    public static String toLowerCase(String s) {
        return (s != null) ? s.toLowerCase() : null;
    }

    public static String getSubjectName(com.sharpdroid.registroelettronico.Databases.Entities.Subject subject) {
        boolean hasUppercase = !subject.getDescription().equals(subject.getDescription().toLowerCase());
        return (hasUppercase) ? subject.getDescription() : WordUtils.capitalizeFully(subject.getDescription());
    }

    public static Media getHypotheticalAverage(MarkSubject markSubject, Mark mark) {
        Media m = new Media();
        m.addMarks(markSubject.getMarks());
        m.addMark(mark);
        return m;
    }

    public static int getPossibileSubjectTarget(double media) {
        if (media < 6)
            return 6;
        else {
            Double m = Math.ceil(media);
            return m.intValue();
        }
    }

    public static boolean writeResponseBodyToDisk(ResponseBody body, File file) {
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1)
                        break;
                    outputStream.write(fileReader, 0, read);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static String getFileNamefromHeaders(Headers headers) {
        String contentd = headers.get("Content-Disposition");
        contentd = contentd.replace("attachment; filename=", "");
        contentd = contentd.replaceAll("\"", "");
        contentd = contentd.trim();
        return contentd;
    }

    public static List<Grade> sortMarksByDate(List<Grade> marks) {
        Collections.sort(marks, (o1, o2) -> o1.getMDate().compareTo(o2.getMDate()));
        return marks;
    }

    public static Map<String, List<Absence>> convertAbsencesToHashmap(List<com.sharpdroid.registroelettronico.Databases.Entities.Absence> absences) {
        Map<String, List<Absence>> hashMap = new HashMap<>();
/*
        //assenze
        for (Absence absence : absences.getAbsences()) {
            String month = month_year.format(absence.getFrom());
            if (hashMap.containsKey(month)) {
                List<Entry> entries = new ArrayList<>(hashMap.get(month));
                entries.add(new AbsenceEntry(absence));
                hashMap.put(month, entries);
            } else {
                hashMap.put(month, Collections.singletonList(new AbsenceEntry(absence)));
            }
        }*/
        return hashMap;
    }

    public static LinkedHashMap<String, List<Entry>> sortByDate(Map<String, List<Entry>> unsort) {
        LinkedHashMap<String, List<Entry>> sort = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>(unsort.keySet());

        //ordina i mesi
        Collections.sort(keys, (s, t1) -> {
            try {
                return month_year.parse(t1).compareTo(month_year.parse(s));
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        List<Entry> entries;

        for (String key : keys) {
            entries = new ArrayList<>(unsort.get(key));

            //ordina valori di ogni mese
            Collections.sort(entries, (entry, t1) -> {
                        if (entry instanceof AbsencesEntry && t1 instanceof AbsencesEntry) {
                            return ((AbsencesEntry) t1).getTime().compareTo(((AbsencesEntry) entry).getTime());
                        } else
                            return 0;
                    }
            );

            sort.put(key, entries);
        }
        return sort;
    }

    public static List<MarkSubject> getMarksOfThisPeriod(List<MarkSubject> markssubject, String p) {
        if (p == null) return markssubject;
        List<MarkSubject> marksSub = new ArrayList<>();
        for (MarkSubject s : markssubject) {
            List<Mark> marks = new ArrayList<>();
            for (Mark m : s.getMarks())
                if (m.getQ().equals(p))
                    marks.add(m);

            if (!marks.isEmpty())
                marksSub.add(new MarkSubject(s.getName(), marks));
        }

        return marksSub;
    }

    public static int[] splitTeachersCodes(String teacherCodes) {
        String[] s = teacherCodes.split(",");
        int[] c = new int[s.length];

        for (int i = 0; i < c.length; i++)
            c[i] = Integer.parseInt(s[i]);

        return c;
    }

    public static String getProfessorOfThisSubject(List<Lesson> lessons) {
        if (!lessons.isEmpty()) {
            return lessons.get(0).getTeacher();
        }
        return "";
    }

    public static boolean isEventTest(SuperAgenda event) {
        String title = event.getAgenda().getNotes().toLowerCase();
        return title.contains("compito") || title.endsWith("compito") || title.endsWith("verifica") || title.contains("verifica ")
                || title.contains("interrogazione scritta") || title.contains("prova ") || title.contains("test ") || title.endsWith("test") || title.contains("verifiche orali");
    }

    public static List<com.github.sundeepk.compactcalendarview.domain.Event> convertEvents(List<SuperAgenda> events) {
        List<com.github.sundeepk.compactcalendarview.domain.Event> list = new ArrayList<>();
        for (SuperAgenda event : events) {
            list.add(new com.github.sundeepk.compactcalendarview.domain.Event(isEventTest(event) ? Color.parseColor("#FF9800") : Color.WHITE, event.getAgenda().getStart().getTime(), null));
        }
        return list;
    }

    public static void fetchDataOfUser(@NotNull Context c) {
        updateSubjects(c);
        updateLessons(c);
        updateFolders(c);
        updateAgenda(c);
        updateAbsence(c);
        updateBacheca(c);
        updateNote(c);
        updatePeriods(c);
        updateMarks(c);
    }

    public static void updateMarks(@NotNull Context c) {
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;

        APIClient.Companion.with(c).getGrades().subscribe(gradeAPI -> {
            Collection<Grade> grades = Collections.emptyList();
            grades.addAll(gradeAPI.getGrades(p));

            SugarRecord.saveInTx(grades);
            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_MARKS_OK, new Collection[]{grades}));
        }, throwable -> {
            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_MARKS_KO, null));
            throwable.printStackTrace();
        });
    }

    public static void updateSubjects(@NotNull Context c) {
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;
        handler.post(() -> {
            NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_SUBJECTS_START, null);
            NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_TEACHERS_START, null);
        });
        APIClient.Companion.with(c).getSubjects().subscribeOn(AndroidSchedulers.mainThread()).subscribe(subjectAPI -> {
            ArrayList<Teacher> allTeachers = new ArrayList<>();

            for (com.sharpdroid.registroelettronico.Databases.Entities.Subject subject : subjectAPI.getSubjects()) {
                allTeachers.addAll(subject.getTeachers());
                for (Teacher t : subject.getTeachers()) {
                    SubjectTeacher obj = new SubjectTeacher(subject, t, p);
                    SugarRecord.deleteAll(SubjectTeacher.class, "PROFILE=? AND SUBJECT=? AND TEACHER=?", String.valueOf(p.getId()), String.valueOf(subject.getId()), String.valueOf(t.getId()));
                    SugarRecord.save(obj);
                }
            }

            SugarRecord.saveInTx(allTeachers);
            SugarRecord.updateInTx(subjectAPI.getSubjects());

            handler.post(() -> {
                NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_SUBJECTS_OK, new Object[]{subjectAPI.getSubjects()});
                NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_TEACHERS_OK, new Object[]{subjectAPI.getSubjects()});
            });
        }, throwable -> {
            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_TEACHERS_KO, null));
            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_SUBJECTS_KO, null));
            throwable.printStackTrace();
        });
    }

    public static void updateLessons(@NotNull Context c) {
        String[] dates = getStartEnd("yyyyMMdd");
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;
        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_LESSONS_START, null));
        APIClient.Companion.with(c).getLessons(dates[0], dates[1])
                .subscribe(l -> {
                    SugarRecord.updateInTx(l.getLessons(p));
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_LESSONS_OK, null));
                }, throwable -> {
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_LESSONS_KO, null));
                    throwable.printStackTrace();
                });
    }

    public static void updateFolders(@NotNull Context c) {
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;
        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_FOLDERS_START, null));
        APIClient.Companion.with(c).getDidactics()
                .subscribe(didacticAPI -> {
                    List<com.sharpdroid.registroelettronico.Databases.Entities.File> files = new LinkedList<>();
                    List<com.sharpdroid.registroelettronico.Databases.Entities.Folder> folders = new LinkedList<>();
                    List<com.sharpdroid.registroelettronico.Databases.Entities.File> dbFiles = SugarRecord.find(com.sharpdroid.registroelettronico.Databases.Entities.File.class, "PROFILE=?", String.valueOf(p.getId()));

                    for (Teacher teacher : didacticAPI.getDidactics()) {
                        if (teacher != null) {
                            for (Folder folder : teacher.getFolders()) {
                                folder.setTeacher(teacher.getId());
                                for (com.sharpdroid.registroelettronico.Databases.Entities.File file : folder.getFiles()) {
                                    file.setFolder(folder.getFolderId());
                                    file.setTeacher(teacher.getId());
                                    file.setProfile(p.getId());
                                    files.add(file);
                                }
                                folder.setFiles(Collections.emptyList());
                                folder.setProfile(p);
                                folders.add(folder);
                            }
                            teacher.setFolders(Collections.emptyList());
                        }
                    }

                    //DO NOT update dbElements that are already saved (updateInTx is replacing all columns)
                    List<com.sharpdroid.registroelettronico.Databases.Entities.File> toRemove = new ArrayList<>();
                    for (com.sharpdroid.registroelettronico.Databases.Entities.File file : files) {
                        for (com.sharpdroid.registroelettronico.Databases.Entities.File dbFile : dbFiles) {
                            if (file.getId() == dbFile.getId()) {
                                toRemove.add(file);
                                break;
                            }
                        }
                    }
                    files.removeAll(toRemove);


                    SugarRecord.deleteAll(Folder.class, "PROFILE=?", String.valueOf(p.getId()));
                    SugarRecord.saveInTx(didacticAPI.getDidactics());
                    SugarRecord.saveInTx(folders);
                    SugarRecord.updateInTx(files); //update otherwise will clean any additional info (path...)


                    //Download informations if not file
                    for (com.sharpdroid.registroelettronico.Databases.Entities.File f : SugarRecord.find(com.sharpdroid.registroelettronico.Databases.Entities.File.class, "PROFILE=? AND TYPE!='file' AND PATH=''", new String[]{String.valueOf(p.getId())})) {
                        if (f.getType().equals("link"))
                            APIClient.Companion.with(c).getAttachmentUrl(f.getId()).subscribe(downloadURL -> {
                                f.setPath(downloadURL.getItem().getLink());
                                SugarRecord.save(f);
                            }, Throwable::printStackTrace);
                        else
                            APIClient.Companion.with(c).getAttachmentTxt(f.getId()).subscribe(downloadTXT -> {
                                f.setPath(downloadTXT.getItem().getText());
                                SugarRecord.save(f);
                            });
                    }
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_FOLDERS_OK, null));
                }, throwable -> {
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_FOLDERS_KO, null));
                    throwable.printStackTrace();
                });

    }

    public static void updateAgenda(@NotNull Context c) {
        String[] dates = getStartEnd("yyyyMMdd");
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;
        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_AGENDA_START, null));
        APIClient.Companion.with(c).getAgenda(dates[0], dates[1])
                .subscribe(agendaAPI -> {
                    SugarRecord.saveInTx(agendaAPI.getAgenda(p));
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_AGENDA_OK, null));
                }, throwable -> {
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_AGENDA_KO, null));
                    throwable.printStackTrace();
                });
    }

    public static void updateAbsence(@NotNull Context c) {
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;
        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_ABSENCES_START, null));
        APIClient.Companion.with(c).getAbsences()
                .subscribe(absenceAPI -> {
                    SugarRecord.saveInTx(absenceAPI.getEvents(p));
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_ABSENCES_OK, null));
                }, throwable -> {
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_ABSENCES_KO, null));
                    throwable.printStackTrace();
                });
    }

    public static void updateBacheca(@NotNull Context c) {
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;
        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_BACHECA_START, null));
        APIClient.Companion.with(c).getBacheca()
                .subscribe(communicationAPI -> {
                    List<Communication> db = SugarRecord.find(Communication.class, "PROFILE=? AND (PATH IS NOT \"\" OR CONTENT IS NOT \"\")", String.valueOf(p.getId()));
                    List<Communication> list = communicationAPI.getCommunications(p);
                    List<Communication> toRemove = new ArrayList<>();

                    for (Communication communication : list) {
                        for (Communication rem : db) {
                            if (rem.getId() == communication.getId()) {
                                toRemove.add(communication);
                                break;
                            }
                        }
                    }

                    //Delete items that are already downloaded
                    list.removeAll(toRemove);
                    toRemove.clear();

                    for (Communication communication : list) {
                        if (communication.getCntStatus().equals("deleted")) {
                            toRemove.add(communication);
                            break;
                        }
                        if (!communication.isRead() || communication.getContent().isEmpty()) {
                            APIClient.Companion.with(c).readBacheca(communication.getEvtCode(), communication.getId()).subscribe(readResponse -> {
                                communication.setRead(true);
                                communication.setContent(readResponse.getItem().getText());
                                SugarRecord.save(communication);
                            });
                            toRemove.add(communication);
                        }
                    }

                    //Delete items that are going to be updated after they are downloaded
                    list.removeAll(toRemove);

                    SugarRecord.saveInTx(list);
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_BACHECA_OK, null));
                }, throwable -> {
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_BACHECA_KO, null));
                    throwable.printStackTrace();
                });
    }

    public static void updateNote(@NotNull Context c) {
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;
        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_NOTES_START, null));
        APIClient.Companion.with(c).getNotes()
                .subscribe(notes -> {
                    SugarRecord.saveInTx(notes.getNotes(p));
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_NOTES_OK, null));
                }, throwable -> {
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_NOTES_KO, null));
                    throwable.printStackTrace();
                });
    }

    public static void updatePeriods(@NotNull Context c) {
        Profile p = Profile.Companion.getProfile(c);
        if (p == null) return;
        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_PERIODS_START, null));
        SugarRecord.deleteAll(Period.class, "PROFILE=?", String.valueOf(p.getId()));
        APIClient.Companion.with(c).getNotes()
                .subscribe(notes -> {
                    SugarRecord.saveInTx(notes.getNotes(p));
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_PERIODS_OK, null));
                }, throwable -> {
                    handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.UPDATE_PERIODS_KO, null));
                    throwable.printStackTrace();
                });
    }

    public static void downloadAttachment(@NotNull Context c, Communication communication) {
        File dir = new File(
                Environment.getExternalStorageDirectory() +
                        File.separator +
                        "Registro Elettronico" + File.separator + "Circolari");

        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_START, new Long[]{communication.getId()}));
        APIClient.Companion.with(c).getBachecaAttachment(communication.getEvtCode(), communication.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String filename = getFileNamefromHeaders(response.headers());
                        if (!dir.exists()) dir.mkdirs();
                        File fileDir = new File(dir, filename);

                        if (fileDir.exists()) {      //File esistente ma non salvato nel db
                            communication.setPath(fileDir.getAbsolutePath());
                            SugarRecord.update(communication);
                            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_OK, new Long[]{communication.getId()}));
                        } else if (writeResponseBodyToDisk(response.body(), fileDir)) {
                            communication.setPath(fileDir.getAbsolutePath());
                            SugarRecord.update(communication);
                            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_OK, new Long[]{communication.getId()}));
                        } else {
                            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_KO, new Long[]{communication.getId()}));
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_KO, new Long[]{communication.getId()}));
                    }
                });
    }

    public static void downloadFile(@NotNull Context c, com.sharpdroid.registroelettronico.Databases.Entities.File f) {
        File dir = new File(
                Environment.getExternalStorageDirectory() +
                        File.separator +
                        "Registro Elettronico" + File.separator + "Didattica");

        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_START, new Long[]{f.getId()}));
        APIClient.Companion.with(c).getAttachmentFile((int) f.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String filename = getFileNamefromHeaders(response.headers());
                        if (!dir.exists()) dir.mkdirs();
                        File fileDir = new File(dir, filename);

                        if (fileDir.exists()) {      //File esistente ma non salvato nel db
                            f.setPath(fileDir.getAbsolutePath());
                            SugarRecord.update(f);
                            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_OK, new Long[]{f.getId()}));
                        } else if (writeResponseBodyToDisk(response.body(), fileDir)) {
                            f.setPath(fileDir.getAbsolutePath());
                            SugarRecord.update(f);
                            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_OK, new Long[]{f.getId()}));
                        } else {
                            handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_KO, new Long[]{f.getId()}));
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        handler.post(() -> NotificationManager.Companion.getInstance().postNotificationName(EventType.DOWNLOAD_FILE_KO, new Long[]{f.getId()}));
                    }
                });
    }

    public static String[] getStartEnd(String format) {
        Calendar from, to;
        from = Calendar.getInstance();
        to = Calendar.getInstance();

        if (from.get(Calendar.MONTH) >= Calendar.SEPTEMBER) { // Prima di gennaio
            to.add(Calendar.YEAR, 1);
        } else {
            from.add(Calendar.YEAR, -1);
        }
        from.set(Calendar.DAY_OF_MONTH, 1);
        from.set(Calendar.MONTH, Calendar.SEPTEMBER);

        to.set(Calendar.DAY_OF_MONTH, 31);
        to.set(Calendar.MONTH, Calendar.AUGUST);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return new String[]{simpleDateFormat.format(from.getTime()), simpleDateFormat.format(to.getTime())};
    }

    public static void addEventToCalendar(Context c, SuperAgenda event) {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, event.getAgenda().getNotes());
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, event.getAgenda().isFullDay());
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getAgenda().getStart().getTime());
        if (!event.getAgenda().isFullDay())
            calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getAgenda().getEnd().getTime());
        c.startActivity(calIntent);
    }

    public static String capitalizeFirst(String a) {
        return a.substring(0, 1).toUpperCase() + a.substring(1);
    }

    public static String getSubjectNameOrProfessorName(Event event, RegistroDB db) {
        String subjectOrProf = TextUtils.isEmpty(event.getMateria_desc()) ? db.getSubjectOrProfessorName(event.getAutore_id()) : event.getMateria_desc();
        return WordUtils.capitalizeFully(TextUtils.isEmpty(subjectOrProf) ? event.getAutore_desc() : subjectOrProf, Delimeters);
    }

    public static String eventToString(SuperAgenda e, String head) {
        return capitalizeFirst(complex.format(e.getAgenda().getStart())) + "\n---" + head + "---\n" + capitalizeFirst(e.getAgenda().getNotes());
    }

    public static List<String> getNamesFromSubjects(List<Subject> subjects) {
        List<String> names = new ArrayList<>();
        for (Subject s : subjects) {
            throw new UnsupportedOperationException("Not yet supported");
            //names.add(getSubjectName(s));
        }
        return names;
    }

    public static int[] getCodesFromSubjects(List<Subject> subjects) {
        int[] codes = new int[subjects.size()];
        for (int i = 0; i < subjects.size(); i++) {
            codes[i] = subjects.get(i).getId();
        }
        return codes;
    }

    public static <T, K> List<T> pairToFirst(List<Pair<T, K>> pairs) {
        List<T> seconds = new ArrayList<>();
        for (Pair<T, K> pair : pairs) {
            seconds.add(pair.first);
        }
        return seconds;
    }

    public static <T, K> List<K> pairToSecond(List<Pair<T, K>> pairs) {
        List<K> seconds = new ArrayList<>();
        for (Pair<T, K> pair : pairs) {
            seconds.add(pair.second);
        }
        return seconds;
    }

    public static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    public static <S extends CharSequence> List<String> capitalizeList(List<S> list) {
        List<String> capitalized = new ArrayList<>();
        for (S s : list) {
            capitalized.add(WordUtils.capitalizeFully(s.toString(), Delimeters));
        }
        return capitalized;
    }

    public static Bitmap AccountImage(String nome) {
        Bitmap src = Bitmap.createBitmap(255, 255, Bitmap.Config.ARGB_8888);
        src.eraseColor(Color.parseColor("#03A9F4"));
        String nomef = "";
        String[] lett = nome.split("\\s+");
        for (String s :
                lett) {
            nomef += s.substring(0, 1).toUpperCase();
        }
        Canvas cs = new Canvas(src);
        Paint tPaint = new Paint();
        float reduce = tPaint.measureText(nomef);
        tPaint.setTextSize(100 - reduce);
        tPaint.setColor(Color.WHITE);
        tPaint.setStyle(Paint.Style.FILL);
        tPaint.setTextAlign(Paint.Align.CENTER);
        float x_coord = src.getWidth() / 2;
        float height = (src.getHeight() / 2) + 33;
        cs.drawText(nomef, x_coord, height, tPaint);
        return src;
    }

    public static String createCookieKey(Cookie cookie) {
        return (cookie.secure() ? "https" : "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name();
    }

    public static int getThemeTextColorSecondary(Context context) {
        int colorAttr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAttr = android.R.attr.textColorSecondary;
        } else {
            //Get colorAccent defined for AppCompat
            colorAttr = context.getResources().getIdentifier("textColorSecondary", "attr", context.getPackageName());
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.data;
    }

    public static void loginFeedback(Throwable error, Context c) {
        error.printStackTrace();
        if (error instanceof HttpException) {
            if (((HttpException) error).code() == 422)
                Toast.makeText(c, R.string.credenziali, Toast.LENGTH_LONG).show();
            else if (((HttpException) error).code() == 400) {
                Toast.makeText(c, "Bad request", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(c, c.getString(R.string.login_msg_failer, error.getLocalizedMessage()), Toast.LENGTH_LONG).show();
        }
    }

    public static void openFile(Context context, java.io.File file) throws ActivityNotFoundException {
        String mime = URLConnection.guessContentTypeFromName(file.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file), mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(intent);

    }

    public static void openLink(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

}

