package castle.components;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.StringMap;
import arc.util.Strings;
import arc.util.Structs;
import castle.Main;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.Player;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Bundle {

    public static final Locale[] supportedLocales;
    private static final ObjectMap<Locale, StringMap> bundles = new ObjectMap<>();
    private static final ObjectMap<Locale, MessageFormat> formats = new ObjectMap<>();

    static {
        Fi[] files = Vars.mods.list().find(mod -> mod.main instanceof Main).root.child("bundles").list();
        supportedLocales = new Locale[files.length + 1];
        supportedLocales[supportedLocales.length - 1] = new Locale("router");

        for (int i = 0; i < files.length; i++) {
            String code = files[i].nameWithoutExtension();
            code = code.substring("bundle_".length());
            if (code.contains("_")) {
                String[] codes = code.split("_");
                supportedLocales[i] = new Locale(codes[0], codes[1]);
            } else {
                supportedLocales[i] = new Locale(code);
            }
        }
    }

    public static Locale defaultLocale() {
        return Structs.find(supportedLocales, l -> l.toString().equals("en"));
    }

    public static String get(String key, Locale locale) {
        StringMap bundle = getOrLoad(locale);
        return bundle != null && bundle.containsKey(key) ? bundle.get(key) : "???" + key + "???";
    }

    public static String format(String key, Locale locale, Object... values) {
        String pattern = get(key, locale);
        MessageFormat format = formats.get(locale);
        if (!Structs.contains(supportedLocales, locale)) {
            format = formats.get(defaultLocale(), () -> new MessageFormat(pattern, defaultLocale()));
            format.applyPattern(pattern);
        } else if (format == null) {
            format = new MessageFormat(pattern, locale);
            formats.put(locale, format);
        } else {
            format.applyPattern(pattern);
        }
        return format.format(values);
    }

    private static StringMap getOrLoad(Locale locale) {
        StringMap bundle = bundles.get(locale);
        if (bundle == null && locale.getDisplayName().equals("router")) {
            StringMap router = new StringMap();
            getOrLoad(defaultLocale()).each((k, v) -> router.put(k, Strings.stripColors(v).replaceAll("[\\d\\D]", String.valueOf(Iconc.blockRouter))));
            bundles.put(locale, bundle = router);
        } else if (bundle == null && Structs.contains(supportedLocales, locale)) {
            bundles.put(locale, bundle = load(locale));
        }
        return bundle != null ? bundle : bundles.get(defaultLocale());
    }

    private static StringMap load(Locale locale) {
        StringMap properties = new StringMap();
        ResourceBundle bundle = ResourceBundle.getBundle("bundles.bundle", locale);
        for (String s : bundle.keySet()) {
            properties.put(s, bundle.getString(s));
        }
        return properties;
    }

    public static Locale findLocale(Player player) {
        Locale locale = Structs.find(supportedLocales, l -> l.toString().equals(player.locale) || player.locale.startsWith(l.toString()));
        return locale != null ? locale : defaultLocale();
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(format(key, findLocale(player), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }
}