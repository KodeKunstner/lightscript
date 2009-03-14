/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.lcdui.*;

/**
 * @author voel
 */
public class Zui extends Canvas implements CommandListener, Order {
    
String names[] = {"a2ps", "AbiSuite-2.4", "aclocal", "acpi-support", "adduser", "aiksaurus",
"alsa", "alsa-base", "ant", "apache2", "apmd", "app-install", "application-registry",
"applications", "applnk", "apport", "apps", "apt", "aptitude", "apturl", "ardour2", "aspell",
"audacity", "autostart", "avahi", "backgrounds", "base-files", "base-passwd", "bash", "binfmts", 
"binfmt-support", "bison", "brasero", "bug", "ca-certificates", "calendar", "classpath",
"CMake", "cm-super", "command-not-found", "common-licenses", "config.kcfg", "consolefonts",
"console-setup", "console-tools", "consoletrans", "cups", "cupsddk", "dbus-1", "debconf",
"debhelper", "debianutils", "defoma", "desktop-directories", "dict", "dictionaries-common",
"directfb-1.0.1", "displayconfig-gtk", "djvu", "doc", "doc-base", "dpkg", "e2fsprogs",
"emacs", "emoticons", "enchant", "epdfview", "epiphany", "et", "evince", "faces", "file",
"file-roller", "firefox", "fonts", "fuse-utils", "games", "gcalctool", "gcj", "gconf",
"gdebi", "gdl", "gdm", "gettext", "gftp", "ggz", "ghostscript", "gimp", "gksu", "glchess",
"glib-2.0", "gnect", "gnibbles", "gnobots2", "gnumeric", "gnupg", "goffice", "graphviz", "groff",
"gst-python", "gstreamer-0.10", "gstreamer-properties", "gtk-doc", "gtk-engines", "gtkmathview",
"gtksourceview-1.0", "guile", "gutenprint", "hal", "hosts", "hotkey-setup", "hplip", "i18n",
"icons", "idl", "ifupdown", "im-switch", "info", "initramfs-tools", "initrd-tools", "initscripts",
"inkscape", "installation-report", "isdn", "iso-codes", "java", "java-common", "javazi", "jockey",
"keymaps", "keyrings", "ladspa", "language-selector", "language-support", "laptop-mode-tools",
"launchpad-integration", "lcdf-typetools", "lftp", "libgda-3.0", "libgksu", "libgphoto2",
"libthai", "lilypond", "linda", "link-grammar", "lintian", "linux-restricted-modules",
"linux-sound-base", "locale", "locale-langpack", "locales", "lua", "man", "man-db", "menu",
"metacity", "mime", "mime-info", "mimelnk", "misc", "mozilla", "mplayer", "myspell", "mysql",
"mysql-common", "nano", "netbeans", "nm-applet", "nvidia-kernel-common", "ogonkify", "omf",
"onboard", "openoffice", "openssl-blacklist", "orage", "ots", "pam", "perl", "perl5", "php5",
"phpmyadmin", "pixmaps", "pkgconfig", "plt", "pmi", "pnm2ppa", "PolicyKit", "popularity-contest",
"postgresql", "powernowd", "ppd", "ppp", "pycentral-data", "pygtk", "pyshared", "pyshared-data",
"python", "python-apt", "python-support", "qt3", "qt4", "R", "readline", "recovery-mode",
"sablevm", "sablevm-classpath", "samba", "sane", "scim", "screen", "scrollkeeper", "services",
"servicetypes", "sgml", "sgml-base", "sgml-data", "snmp", "software-properties", "sounds", 
"squeak", "ssl-cert", "synaptic", "sysklogd", "system-config-printer", "system-tools-backends-2.0",
"tabset", "tasksel", "tcltk", "templates", "Terminal", "terminfo", "tex4ht", "tex-common",
"texinfo", "texlive-base", "texlive-bin", "texmf", "texmf-texlive", "themes", "thumbnailers",
"Thunar", "thunderbird", "totem", "ubufox", "ubuntu-artwork", "ufw", "update-manager",
"update-notifier", "usb", "videomodes", "vim", "vinagre", "vlc", "vte", "w3m", "wallpapers",
"wine", "X11", "xfce4", "xfce4-menueditor", "xfce4-notes-plugin", "xfce-mcs-plugins", "xfwm4",
"xml", "xml-core", "xscreensaver", "xserver-xorg", "xsessions", "xubuntu-docs", "zenity"};

    public boolean leq(Object o1, Object o2) {
        return o1 != null && o2 == null || ((ZuiObject)o1).z > ((ZuiObject)o2).z;
    }

    int count = names.length;
    ZuiObject objs[] = new ZuiObject[count];

    public Zui() {
        try {
	    // Set up this canvas to listen to command events
	    setCommandListener(this);
	    // Add the Exit command
	    addCommand(new Command("Exit", Command.EXIT, 1));
        } catch(Exception e) {
            e.printStackTrace();
        }
        for(int i = 0; i < count; i++) {
            int prio = Util.random(10000);
            int pos = Util.hilbert(i*65535 / count);
            int x = pos >>> 16; 
            int y = pos & 0xffff;
            x -= 128;
            y -= 128;
            objs[i] = new ZuiObject(names[i], x, y, prio);
        }
        Util.qsort(objs, this);
        for(int i = 0; i < count; i++) {
            System.out.println(objs[i].z);
        }
    } 
    
    public int x0 = 0;
    public int y0 = 0;
    public int scale = (1<< 16);
    /**
     * paint
     */
    public void paint(Graphics g) {
        g.setColor(192, 192, 192);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        for(int i = 0; i <count; i++) {
            ZuiObject zo = objs[i];
            int x = ((zo.x + x0) * scale) >> 16;
            int y = ((zo.y + y0) * scale) >> 16;
            g.drawImage(zo.img, x + this.getWidth()/2, y + this.getHeight()/2, 
                    Graphics.HCENTER|Graphics.VCENTER);
        }
    }
    
    /**
     * Called when a key is pressed.
     */
    protected  void keyPressed(int keyCode) {
        switch(keyCode) {
            case Zui.KEY_NUM1:
                scale = (scale * 5) / 4 + 1;
                break;
            case Zui.KEY_NUM3:
                scale = (scale * 4) / 5 + 1;
                break;
            case Zui.KEY_NUM2:
                y0  += (1<< 19) / scale + 1;
                break;
            case Zui.KEY_NUM5:
                y0  -= (1<< 19) / scale + 1;
                break;
            case Zui.KEY_NUM4:
                x0  += (1<< 19) / scale + 1;
                break;
            case Zui.KEY_NUM6:
                x0  -= (1<< 19) / scale + 1;
                break;
        }
        this.repaint();
    }
    
    /**
     * Called when a key is released.
     */
    protected  void keyReleased(int keyCode) {
    }

    /**
     * Called when a key is repeated (held down).
     */
    protected  void keyRepeated(int keyCode) {
    }
    
    /**
     * Called when the pointer is dragged.
     */
    protected  void pointerDragged(int x, int y) {
    }

    /**
     * Called when the pointer is pressed.
     */
    protected  void pointerPressed(int x, int y) {
    }

    /**
     * Called when the pointer is released.
     */
    protected  void pointerReleased(int x, int y) {
    }
    
    /**
     * Called when action should be handled
     */
    public void commandAction(Command command, Displayable displayable) {
    }


}