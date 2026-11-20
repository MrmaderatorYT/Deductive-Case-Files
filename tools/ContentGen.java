import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.ClueModel;
import com.ccs.deductive_case_files.game.ClueModel.Constraint;
import com.ccs.deductive_case_files.game.PuzzleSolver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Development-time content generator (not shipped). Builds cases 31..120: a uniquely solvable
 * puzzle from Generator plus Ukrainian and English texts composed from tools/vocab.txt.
 * The result is appended to the asset files after a marker line, replacing earlier output.
 * Usage: java ContentGen app/src/main/assets tools/vocab.txt
 */
public final class ContentGen {
    static final String MARK = "# ---- generated cases (tools/ContentGen.java) ----";

    static final class Setting {
        String scene;
        List<String[]> places = new ArrayList<>(), items = new ArrayList<>(), rolesM = new ArrayList<>(),
                rolesF = new ArrayList<>(), loots = new ArrayList<>();
    }

    static final List<Setting> SETTINGS = new ArrayList<>();
    static final List<String[]> NAMES = new ArrayList<>();

    // per-case context
    static CaseModel m;
    static String[][] names, roles, places, items;
    static int hour0;
    static boolean en;

    public static void main(String[] args) throws Exception {
        readVocab(Paths.get(args[1]));
        StringBuilder dat = new StringBuilder(), uk = new StringBuilder(), enOut = new StringBuilder();
        String[] kinds = ("MATRIX ALIBI CIPHER MATRIX CONTRADICTION DOCUMENT MATRIX VISUAL SEQUENCE ALIBI "
                + "MATRIX CIPHER CONTRADICTION MATRIX DOCUMENT VISUAL MATRIX SEQUENCE ALIBI CIPHER "
                + "MATRIX CONTRADICTION MATRIX ALIBI CIPHER MATRIX CONTRADICTION DOCUMENT VISUAL SEQUENCE").split(" ");
        int count = 0;
        for (int diff = 0; diff < 3; diff++) {
            for (int i = 0; i < 30; i++, count++) {
                int id = 31 + count, n, cats, lvl = 1, min, max;
                if (diff == 0) {
                    n = i < 12 ? 3 : 4;
                    cats = 3;
                } else if (diff == 1) {
                    n = i < 24 ? 4 : 5;
                    cats = i >= 12 && i < 24 ? 4 : 3;
                } else {
                    n = i >= 14 && i < 20 ? 4 : 5;
                    cats = i < 14 ? 3 : 4;
                    lvl = 2;
                }
                if (n == 3) {
                    min = 3;
                    max = 5;
                } else if (n == 4) {
                    min = cats == 3 ? 5 : 6;
                    max = cats == 3 ? 8 : 10;
                } else {
                    min = cats == 3 ? 8 : 9;
                    max = cats == 3 ? 12 : 14;
                }
                String kind = kinds[i];
                Setting st = SETTINGS.get(count % SETTINGS.size());
                String[] loot = st.loots.get((count / SETTINGS.size()) % st.loots.size());
                Object[] spec = {id, diff, n, cats, cats == 4 ? 3 : -1, kind, st.scene, min, max, lvl};
                int lock = -1;
                m = null;
                for (long seed = id * 7919L; ; seed++) {
                    m = Generator.attempt(spec, new Random(seed));
                    if (m == null) continue;
                    lock = "MATRIX".equals(kind) ? 0 : findLock(kind);
                    if (lock >= 0) break;
                }
                Random rnd = new Random(id * 31L + 5);
                pickVocab(st, rnd);
                int[][] sol = PuzzleSolver.solve(m);
                int culprit = m.culprit(sol);
                String[] task = "MATRIX".equals(kind) ? null : new String[2];

                dat.append("\n[").append(id).append("]\nd=").append(diff).append("\nk=").append(kind)
                        .append("\ns=").append(st.scene).append("\nn=").append(n).append("\nc=").append(cats)
                        .append("\no=").append(m.ordered).append("\nkey=").append(ClueModel.name(m.key)).append('\n');
                for (ClueModel c : m.clues) dat.append("q=").append(c.spec()).append('\n');

                for (int lang = 0; lang < 2; lang++) {
                    en = lang == 1;
                    StringBuilder o = en ? enOut : uk;
                    o.append("\n[").append(id).append("]\n");
                    o.append("title=").append(loot[en ? 1 : 0]).append('\n');
                    String word = en ? new String[]{"three", "four", "five"}[n - 3] : new String[]{"троє", "четверо", "п'ятеро"}[n - 3];
                    o.append("intro=").append(loot[en ? 3 : 2]).append(en
                            ? " There are " + word + " suspects: each was in one place and had one thing."
                            : " Підозрюваних " + word + ": кожен був в одному місці й мав при собі одну річ.");
                    if (cats == 4) o.append(en ? " Each was seen only once — at a certain time."
                            : " Кожен потрапив на очі свідкам лише раз — у певний час.");
                    o.append('\n');
                    String loc = places[ClueModel.idx(m.key)][en ? 3 : 1];
                    o.append("fact=").append(en
                            ? "It all happened " + loc + ", and only one person was there all evening. That person is guilty."
                            : "Усе сталося " + loc + ", і за весь вечір там побувала лише одна людина. Вона і винна.").append('\n');
                    o.append("A=").append(en ? "Suspects" : "Підозрювані");
                    for (int k = 0; k < n; k++) o.append('|').append(names[k][en ? 2 : 1]).append(':').append(roles[k][en ? 2 : 1]);
                    o.append("\nB=").append(en ? "Places" : "Місця");
                    for (int k = 0; k < n; k++) o.append('|').append(places[k][en ? 2 : 0]);
                    o.append("\nC=").append(en ? "Items" : "Предмети");
                    for (int k = 0; k < n; k++) o.append('|').append(items[k][en ? 3 : 0]);
                    if (cats == 4) {
                        o.append("\nD=").append(en ? "Time" : "Час");
                        for (int k = 0; k < n; k++) o.append('|').append(hour0 + k).append(":00");
                    }
                    o.append('\n');
                    for (int k = 0; k < m.clues.size(); k++)
                        o.append('q').append(k).append('=').append(clue(m.clues.get(k).parts[0])).append('\n');
                    if (task != null) task(o, kind, lock, new Random(id * 131L + 3), task);
                    boolean f = fem(culprit);
                    String nm = names[culprit][en ? 2 : 1];
                    String item = en ? low(items[sol[2][culprit]][3]) : items[sol[2][culprit]][1];
                    String out;
                    if (en) {
                        out = id % 3 == 0 ? nm + " was " + loc + " — the only one who could have done it, with the " + item
                                + " at hand. The missing item was found among " + (f ? "her" : "his") + " things."
                                : id % 3 == 1 ? "All the evidence pointed one way: only " + nm + " was " + loc
                                + ". After a search " + (f ? "she" : "he") + " confessed to everything."
                                : nm + " denied it to the last, but nobody else was " + loc
                                + ". What was taken has been returned to its owners.";
                    } else {
                        out = id % 3 == 0 ? nm + " " + g(f, "був", "була") + " " + loc + " — " + g(f, "єдиний", "єдина")
                                + ", хто міг це зробити. При собі " + g(f, "мав", "мала") + " " + item
                                + "; зникле знайшли серед " + g(f, "його", "її") + " речей."
                                : id % 3 == 1 ? "Усі докази зійшлися на одному: " + loc + " " + g(f, "був", "була") + " тільки "
                                + nm + ". Після обшуку " + g(f, "він", "вона") + " в усьому " + g(f, "зізнався", "зізналася") + "."
                                : nm + " до останнього " + g(f, "заперечував", "заперечувала") + " провину, але " + loc
                                + " більше нікого не було. Зникле повернули власникам.";
                    }
                    o.append("outro=").append(out).append('\n');
                }
                if (task != null) {
                    dat.append("lock=").append(lock).append("\nt=").append(task[0]).append("\nv=").append(task[1]).append('\n');
                }
                System.err.println("generated " + id + " " + kind + " " + n + "x" + cats + " clues=" + m.clues.size());
            }
        }
        merge(Paths.get(args[0], "cases.dat"), dat);
        merge(Paths.get(args[0], "cases_uk.txt"), uk);
        merge(Paths.get(args[0], "cases_en.txt"), enOut);
    }

    static void merge(Path p, StringBuilder add) throws Exception {
        String s = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        int at = s.indexOf(MARK);
        if (at >= 0) s = s.substring(0, at);
        s = s.replaceAll("\\s+$", "") + "\n\n" + MARK + "\n" + add;
        Files.write(p, s.getBytes(StandardCharsets.UTF_8));
    }

    static void readVocab(Path p) throws Exception {
        Setting cur = null;
        for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            if (line.charAt(0) == '@') {
                cur = new Setting();
                cur.scene = line.substring(1);
                SETTINGS.add(cur);
                continue;
            }
            String[] f = line.substring(2).split("\\|");
            switch (line.charAt(0)) {
                case 'N':
                    NAMES.add(f);
                    break;
                case 'P':
                    cur.places.add(f);
                    break;
                case 'I':
                    cur.items.add(f);
                    break;
                case 'R':
                    ("m".equals(f[0]) ? cur.rolesM : cur.rolesF).add(f);
                    break;
                case 'L':
                    cur.loots.add(f);
                    break;
                default:
                    break;
            }
        }
    }

    static void pickVocab(Setting st, Random rnd) {
        int n = m.n;
        List<String[]> nm = new ArrayList<>(NAMES), pl = new ArrayList<>(st.places), it = new ArrayList<>(st.items),
                rm = new ArrayList<>(st.rolesM), rf = new ArrayList<>(st.rolesF);
        Collections.shuffle(nm, rnd);
        Collections.shuffle(pl, rnd);
        Collections.shuffle(it, rnd);
        Collections.shuffle(rm, rnd);
        Collections.shuffle(rf, rnd);
        names = new String[n][];
        roles = new String[n][];
        places = new String[n][];
        items = new String[n][];
        String initials = "";
        int k = 0, im = 0, jf = 0;
        for (String[] cand : nm) {
            if (k == n) break;
            String ini = cand[1].substring(0, 1) + cand[2].substring(0, 1);
            if (initials.contains(cand[1].substring(0, 1)) || initials.contains(cand[2].substring(0, 1))) continue;
            initials += ini;
            names[k] = cand;
            roles[k] = "f".equals(cand[0]) ? rf.get(jf++) : rm.get(im++);
            k++;
        }
        for (int i = 0; i < n; i++) {
            places[i] = pl.get(i);
            items[i] = it.get(i);
        }
        hour0 = new int[]{13, 17, 19}[rnd.nextInt(3)];
    }

    /** Index of a clue the evidence task can reveal, or -1. */
    static int findLock(String kind) {
        for (int pass = 0; pass < 2; pass++)
            for (int i = 0; i < m.clues.size(); i++) {
                Constraint c = m.clues.get(i).parts[0];
                if (c.type != ClueModel.NOT || ClueModel.cat(c.a[0]) != 0) continue;
                int cb = ClueModel.cat(c.a[1]);
                if ("SEQUENCE".equals(kind) ? cb == 2 : "CIPHER".equals(kind) ? cb == (pass == 0 ? 1 : 2) : cb == 1) return i;
            }
        return -1;
    }

    // ------------------------------------------------------------------ phrases

    static String g(boolean f, String male, String female) {
        return f ? female : male;
    }

    static String cap(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static String low(String s) {
        return s.toLowerCase();
    }

    static boolean fem(int e) {
        return ClueModel.cat(e) == 0 && "f".equals(names[ClueModel.idx(e)][0]);
    }

    static String time(int e) {
        return (hour0 + ClueModel.idx(e)) + ":00";
    }

    static String who(int e) {
        int i = ClueModel.idx(e);
        switch (ClueModel.cat(e)) {
            case 0:
                return names[i][en ? 2 : 1];
            case 1:
                return en ? "the person " + places[i][3] : "той, хто був " + places[i][1];
            case 2:
                return en ? "the person with the " + low(items[i][3]) : "той, хто мав " + items[i][1];
            default:
                return en ? "the person seen at " + time(e) : "той, кого бачили о " + time(e);
        }
    }

    /** Subject phrase; Ukrainian needs a comma after a relative clause. */
    static String subj(int e) {
        return cap(who(e)) + (!en && ClueModel.cat(e) > 0 ? "," : "");
    }

    static String pred(int e, boolean f) {
        int i = ClueModel.idx(e);
        switch (ClueModel.cat(e)) {
            case 1:
                return en ? "was " + places[i][3] : g(f, "був ", "була ") + places[i][1];
            case 2:
                return en ? "had the " + low(items[i][3]) : g(f, "мав ", "мала ") + items[i][1];
            default:
                return en ? "was seen at " + time(e) : g(f, "потрапив", "потрапила") + " на очі свідкам о " + time(e);
        }
    }

    static String neg(int e, boolean f) {
        int i = ClueModel.idx(e);
        switch (ClueModel.cat(e)) {
            case 1:
                return en ? "was not " + places[i][3] : g(f, "не був ", "не була ") + places[i][1];
            case 2:
                return en ? "did not have the " + low(items[i][3]) : g(f, "не мав ", "не мала ") + items[i][2];
            default:
                return en ? "was not seen at " + time(e) : g(f, "потрапив", "потрапила") + " на очі свідкам не о " + time(e);
        }
    }

    static String clue(Constraint c) {
        int[] a = c.a;
        switch (c.type) {
            case ClueModel.IS:
                return subj(a[0]) + " " + pred(a[1], fem(a[0])) + ".";
            case ClueModel.NOT:
                if (ClueModel.cat(a[0]) == 1 && ClueModel.cat(a[1]) == 2) {
                    String[] p = places[ClueModel.idx(a[0])], it = items[ClueModel.idx(a[1])];
                    return en ? "The " + low(it[3]) + " was not " + p[3] + "." : cap(p[1]) + " " + it[2] + " не було.";
                }
                return subj(a[0]) + " " + neg(a[1], fem(a[0])) + ".";
            case ClueModel.EITHER: {
                boolean same = ClueModel.cat(a[1]) == ClueModel.cat(a[2]), f = fem(a[0]);
                if (en) return cap(who(a[0])) + " either " + pred(a[1], f) + " or " + pred(a[2], f) + (same ? "." : " — but not both.");
                return subj(a[0]) + " або " + pred(a[1], f) + ", або " + pred(a[2], f) + (same ? "." : " — але не те й інше разом.");
            }
            case ClueModel.PAIR: {
                boolean f = fem(a[0]) && fem(a[1]);
                if (en) return cap(who(a[0])) + " and " + who(a[1]) + ": one of them " + pred(a[2], f) + ", the other " + pred(a[3], f) + ".";
                return subj(a[0]) + " і " + who(a[1]) + (ClueModel.cat(a[1]) > 0 ? ": " : ": ") + g(f, "хтось із них ", "одна з них ") + pred(a[2], f)
                        + ", а " + g(f, "інший ", "інша ") + pred(a[3], f) + ".";
            }
            case ClueModel.DIFF:
                return (en ? "These are three different people: " : "Це три різні людини: ") + who(a[0]) + ", " + who(a[1])
                        + (en ? " and " : (ClueModel.cat(a[1]) > 0 ? "," : "") + " і ") + who(a[2]) + ".";
            case ClueModel.LT:
                return en ? cap(who(a[0])) + " was seen earlier than " + who(a[1]) + "."
                        : subj(a[0]) + " " + g(fem(a[0]), "потрапив", "потрапила") + " на очі свідкам раніше, ніж " + who(a[1]) + ".";
            default:
                return en ? cap(who(a[1])) + " was seen exactly one hour after " + who(a[0]) + "."
                        : subj(a[1]) + " " + g(fem(a[1]), "потрапив", "потрапила") + " на очі свідкам рівно через годину після того, як їм "
                        + g(fem(a[0]), "трапився ", "трапилась ") + who(a[0]) + ".";
        }
    }

    // ------------------------------------------------------------------ evidence tasks

    static void kv(StringBuilder o, String k, String v) {
        o.append(k).append('=').append(v.replace("\n", "\\n")).append('\n');
    }

    /** Writes the task texts; task[0]/task[1] receive the structure lines (same for both languages). */
    static void task(StringBuilder o, String kind, int lock, Random rnd, String[] task) {
        Constraint lc = m.clues.get(lock).parts[0];
        int pa = ClueModel.idx(lc.a[0]), ob = ClueModel.idx(lc.a[1]);
        boolean f = fem(lc.a[0]);
        String a = names[pa][en ? 2 : 1], clueText = clue(lc);
        int xi = (pa + 1) % m.n, yi = (pa + 2) % m.n;
        String x = names[xi][en ? 2 : 1], y = names[yi][en ? 2 : 1];
        boolean fx = "f".equals(names[xi][0]), fy = "f".equals(names[yi][0]);
        String[] pl = places[ob % m.n];
        String loc = pl[en ? 3 : 1];
        int h = 19 + rnd.nextInt(3);
        task[1] = "";
        switch (kind) {
            case "ALIBI": {
                int pv = rnd.nextInt(3);
                String proof = en
                        ? new String[]{"the lobby camera recording from " + (h - 1) + ":50 to " + h + ":40, with no gap",
                        "a call from a landline that lasted from " + (h - 1) + ":55 to " + h + ":35",
                        "the minutes of a meeting " + (f ? "she" : "he") + " chaired from " + (h - 1) + ":45 to " + h + ":45"}[pv]
                        : new String[]{"запис камери в холі від " + (h - 1) + ":50 до " + h + ":40, без жодної перерви",
                        "телефонна розмова з міського апарата, що тривала від " + (h - 1) + ":55 до " + h + ":35",
                        "протокол засідання, де " + g(f, "він головував", "вона головувала") + " від " + (h - 1) + ":45 до " + h + ":45"}[pv];
                kv(o, "tt", en ? "Checking alibis" : "Перевірка алібі");
                kv(o, "tb", en
                        ? "Someone was " + loc + " from " + h + ":00 to " + h + ":30.\n\n" + a + ": “I was in plain sight of everyone at that time.” Confirmation: "
                        + proof + ".\n\n" + x + ": “I was alone, nobody can confirm it.”\n\n" + y + ": “I don't remember the exact time.”"
                        : cap(loc) + " хтось перебував від " + h + ":00 до " + h + ":30.\n\n" + a + ": «У цей час я " + g(f, "був", "була")
                        + " у всіх на очах». Підтвердження: " + proof + ".\n\n" + x + ": «Я " + g(fx, "був", "була")
                        + " на самоті, підтвердити нікому».\n\n" + y + ": «Точного часу я не пам'ятаю».");
                kv(o, "tq", en ? "Who has a confirmed alibi for the whole interval?" : "Хто має підтверджене алібі на весь проміжок?");
                task[0] = "CHOICE " + options(o, rnd, a, x, y);
                kv(o, "tk", (en ? "The alibi is confirmed for the whole interval. " : "Алібі підтверджене на весь проміжок. ") + clueText);
                break;
            }
            case "CONTRADICTION":
                kv(o, "tt", en ? "Words against facts" : "Слова проти фактів");
                kv(o, "tb", en
                        ? "Established: only one person was " + loc + " that evening, and that person signed the visitors' log with the left hand. According to the medical card, "
                        + a + " is right-handed.\n\n" + a + ": “The signature in the log is mine, I was the one " + loc + ".”\n\n" + x
                        + ": “I heard nothing suspicious.”\n\n" + y + ": “I did not look at the clock.”"
                        : "Встановлено: " + loc + " того вечора побувала лише одна людина, і підпис у журналі відвідувачів вона поставила лівою рукою. За медичною карткою, "
                        + a + " — правша.\n\n" + a + ": «Підпис у журналі мій, " + loc + " " + g(f, "був", "була") + " саме я».\n\n" + x
                        + ": «Я нічого підозрілого не " + g(fx, "чув", "чула") + "».\n\n" + y + ": «Я не " + g(fy, "дивився", "дивилася") + " на годинник».");
                kv(o, "tq", en ? "Who is not telling the truth?" : "Хто говорить неправду?");
                task[0] = "CHOICE " + options(o, rnd, a, x, y);
                kv(o, "tk", (en ? "A right-hander could not have signed with the left hand. " : "Правша не міг лишити підпис лівою рукою. ") + clueText);
                break;
            case "DOCUMENT":
                kv(o, "tt", en ? "The lock log" : "Журнал замка");
                kv(o, "tb", en
                        ? "ELECTRONIC LOCK · " + pl[2].toUpperCase() + "\n" + h + ":12 — card No. 4 — entry\n" + h + ":31 — card No. 4 — exit\n\nCard holders: No. 1 — "
                        + a + ", No. 2 — " + x + ", No. 3 — " + y + ", No. 4 — guest card, no name.\nCard No. 1 was not used once all evening, and " + a + " had no other."
                        : "ЕЛЕКТРОННИЙ ЗАМОК · " + pl[0].toUpperCase() + "\n" + h + ":12 — картка №4 — вхід\n" + h + ":31 — картка №4 — вихід\n\nВласники карток: №1 — "
                        + a + ", №2 — " + x + ", №3 — " + y + ", №4 — гостьова, без імені.\nКартка №1 за весь вечір не спрацювала жодного разу, а іншої " + a
                        + " не " + g(f, "мав", "мала") + ".");
                kv(o, "tq", en ? "What follows from the log?" : "Що випливає з журналу?");
                task[0] = "CHOICE " + options(o, rnd,
                        en ? a + " did not go in there" : a + " туди не " + g(f, "заходив", "заходила"),
                        en ? a + " went in there at " + h + ":12" : a + " " + g(f, "заходив", "заходила") + " туди о " + h + ":12",
                        en ? "Nobody went in there" : "Туди не заходив ніхто");
                kv(o, "tk", (en ? "The card was never used. " : "Картка не спрацювала жодного разу. ") + clueText);
                break;
            case "VISUAL":
                if (m.id % 2 == 0) {
                    int ch = 1 + rnd.nextInt(10), mm = new int[]{10, 20, 25, 35, 40, 50}[rnd.nextInt(6)];
                    String t0 = ch + ":" + mm, sw = (mm / 5) + ":" + String.format("%02d", (ch * 5) % 60);
                    String t2 = (ch + 1) + ":" + mm, t3 = (ch == 1 ? 12 : ch - 1) + ":" + mm;
                    if (sw.equals(t0) || sw.equals(t2) || sw.equals(t3)) sw = (ch + 2 > 12 ? ch - 2 : ch + 2) + ":" + mm;
                    kv(o, "tt", en ? "The stopped clock" : "Зупинений годинник");
                    kv(o, "tb", en
                            ? "The clock hanging " + loc + " stopped the moment it was knocked by the person who was there. From " + ch + ":00 to " + (ch + 1)
                            + ":00 " + a + " sat in plain sight of everyone somewhere else entirely."
                            : "Годинник, що висить " + loc + ", зупинився тієї миті, коли його зачепила людина, яка там була. " + a + " від " + ch + ":00 до "
                            + (ch + 1) + ":00 " + g(f, "сидів", "сиділа") + " у всіх на очах зовсім в іншому місці.");
                    kv(o, "tq", en ? "At what time did the clock stop?" : "О котрій зупинився годинник?");
                    task[0] = "CHOICE " + options(o, rnd, t0, sw, t2, t3);
                    task[1] = "CLOCK:" + ch + ":" + mm;
                    kv(o, "tk", (en ? "The hands froze at " + t0 + ", while " + a + " was in plain sight. "
                            : "Стрілки завмерли о " + t0 + ", коли " + a + " " + g(f, "був", "була") + " у всіх на очах. ") + clueText);
                } else {
                    int k = 1 + rnd.nextInt(6);
                    List<Integer> others = new ArrayList<>();
                    for (int r = 1; r <= 6; r++) if (r != k) others.add(r);
                    Collections.shuffle(others, rnd);
                    kv(o, "tt", en ? "Footprints on the floor" : "Сліди на підлозі");
                    kv(o, "tb", en
                            ? "On the plan, room " + k + " is the place marked “" + pl[2] + "”. The round marks were left by the shoes " + a
                            + " wears; the square ones by the night watchman's boots."
                            : "На плані кімната " + k + " — це місце, позначене як «" + pl[0] + "». Круглі сліди лишило взуття, яке носить " + a
                            + "; квадратні — чоботи нічного сторожа.");
                    kv(o, "tq", en ? "Whose footprints pass through room " + k + "?" : "Чиї сліди проходять через кімнату " + k + "?");
                    kv(o, "to", en ? "The round ones|The square ones|Both" : "Круглі|Квадратні|І ті, й інші");
                    task[0] = "CHOICE 1";
                    task[1] = "TRAIL:" + others.get(0) + "-" + others.get(1) + "-" + others.get(2) + ":" + others.get(3) + "-" + k + "-" + others.get(4);
                    kv(o, "tk", (en ? "There are no round marks in room " + k + ". " : "Круглих слідів у кімнаті " + k + " немає. ") + clueText);
                }
                break;
            case "SEQUENCE": {
                String[] it = items[ob];
                kv(o, "tt", en ? "The duty officer's statement" : "Покази чергового");
                kv(o, "tb", en
                        ? "The cards with the duty officer's statement got shuffled. Restore the order: the log entry came last, and the thing left its place only after "
                        + a + " had gone."
                        : "Картки з показами чергового перемішалися. Відновіть порядок: запис у журналі зроблено останнім, а річ зникла з місця вже після того, як "
                        + a + " " + g(f, "пішов", "пішла") + ".");
                kv(o, "tq", en ? "In what order did the events happen?" : "У якому порядку відбулися події?");
                kv(o, "to", en
                        ? "The " + low(it[3]) + " is still in its place — the duty officer sees it|" + a
                        + " walks past the duty officer empty-handed and does not come back|The duty officer notices the " + low(it[3])
                        + " is no longer in its place|The duty officer enters the loss in the log"
                        : it[0] + " ще на своєму місці — це бачить черговий|" + a
                        + " проходить повз чергового з порожніми руками й більше не повертається|Черговий помічає, що " + it[2]
                        + " на місці вже немає|Черговий записує пропажу в журнал");
                task[0] = "ORDER";
                kv(o, "tk", (en ? "The thing disappeared after " + a + " had left empty-handed. "
                        : "Річ зникла вже після того, як " + a + " " + g(f, "пішов", "пішла") + " з порожніми руками. ") + clueText);
                break;
            }
            default: { // CIPHER
                String plain = strip(clueText);
                int cb = ClueModel.cat(lc.a[1]);
                String d1 = strip(clue(new Constraint(ClueModel.NOT, new int[]{lc.a[0], ClueModel.ent(cb, (ob + 1) % m.n)})));
                String d2 = strip(clue(new Constraint(ClueModel.NOT, new int[]{ClueModel.ent(0, xi), lc.a[1]})));
                int r = rnd.nextInt(5);
                String spec = r < 3 ? "CAESAR:" + (r + 1) : r == 3 ? "ATBASH" : "NUMBERS";
                // the same spec must be chosen for both languages: decide from the Ukrainian length only
                if ("NUMBERS".equals(spec) && m.id % 2 == 0) spec = "CAESAR:2";
                String key;
                if (spec.startsWith("CAESAR")) {
                    String k = spec.substring(7);
                    key = en ? "Cipher key: every letter is shifted " + k + " forward in the alphabet. To read it, shift each letter " + k + " back."
                            : "Ключ до шифру: кожну літеру зсунуто на " + k + " вперед за абеткою. Щоб прочитати, зсуньте кожну літеру на " + k + " назад.";
                } else if ("ATBASH".equals(spec)) {
                    key = en ? "Cipher key: the alphabet is mirrored. The first letter becomes the last, the second becomes the last but one, and so on."
                            : "Ключ до шифру: абетку віддзеркалено. Перша літера стає останньою, друга — передостанньою і так далі.";
                } else {
                    key = en ? "Cipher key: every number is the position of a letter in the alphabet (A — 1, B — 2, C — 3…). Words are separated by slashes."
                            : "Ключ до шифру: кожне число — це номер літери в абетці (А — 1, Б — 2, В — 3…). Слова розділено скісними рисками.";
                }
                kv(o, "tt", en ? "A note in cipher" : "Шифрована записка");
                kv(o, "tb", key);
                kv(o, "tp", plain);
                kv(o, "tq", en ? "What does the note say?" : "Що написано в записці?");
                task[0] = "CHOICE " + options(o, rnd, plain, d1, d2);
                task[1] = spec;
                kv(o, "tk", en ? "The note says: “" + clueText + "”" : "У записці сказано: «" + clueText + "»");
            }
        }
    }

    static String strip(String s) {
        return s.substring(0, s.length() - 1).toUpperCase();
    }

    /** Writes shuffled options (first argument is the right one); returns its index. The shuffle depends only on rnd. */
    static int options(StringBuilder o, Random rnd, String... opts) {
        int n = opts.length, shift = rnd.nextInt(n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(i > 0 ? "|" : "").append(opts[(i - shift + n) % n]);
        kv(o, "to", sb.toString());
        return shift;
    }
}
