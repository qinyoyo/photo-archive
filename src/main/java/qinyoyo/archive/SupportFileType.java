package qinyoyo.archive;

import java.util.HashSet;
import java.util.Set;

public class SupportFileType {
    static final Set<String> EXTS = new HashSet<String>() {{
        String [] ss = {
                "360",
                "3FR",
                "3G2", "3GP2",
                "3GP", "3GPP",
                "A",
                "AA",
                "AAE",
                "AAX",
                "ACR",
                "AFM", "ACFM", "AMFM",
                "AI", "AIT",
                "AIFF", "AIF", "AIFC",
                "APE",
                "ARQ",
                "ARW",
                "ASF",
                "AVI",
                "AVIF",
                "BMP", "DIB",
                "BPG",
                "BTF",
                "CHM",
                "COS",
                "CR2",
                "CR3",
                "CRM",
                "CRW", "CIFF",
                "CS1",
                "CSV",
                "CZI",
                "DCM", "DC3", "DIC", "DICM",
                "DCP",
                "DCR",
                "DFONT",
                "DIVX",
                "DJVU", "DJV",
                "DNG",
                "DOC", "DOT",
                "DOCX", "DOCM",
                "DOTX", "DOTM",
                "DPX",
                "DR4",
                "DSS", "DS2",
                "DYLIB",
                "DV",
                "DVB",
                "DVR-MS",
                "EIP",
                "EPS", "EPSF", "PS",
                "EPUB",
                "ERF",
                "EXE", "DLL",
                "EXIF",
                "EXR",
                "EXV",
                "F4A", "F4B", "F4P", "F4V",
                "FFF",
                "FFF",
                "FITS",
                "FLA",
                "FLAC",
                "FLIF",
                "FLV",
                "FPF",
                "FPX",
                "GIF",
                "GPR",
                "GZ", "GZIP",
                "HDP", "WDP", "JXR",
                "HDR",
                "HEIC", "HEIF", "HIF",
                "HTML", "HTM", "XHTML",
                "ICC", "ICM",
                "ICS", "ICAL",
                "IDML",
                "IIQ",
                "IND", "INDD", "INDT",
                "INSP",
                "INSV",
                "INX",
                "ISO",
                "ITC",
                "J2C", "J2K", "JPC",
                "JP2", "JPF", "JPM", "JPX",
                "JPEG", "JPG", "JPE",
                "JSON",
                "K25",
                "KDC",
                "KEY", "KTH",
                "LA",
                "LFP", "LFR",
                "LNK",
                "LRV",
                "M2TS", "MTS", "M2T", "TS",
                "M4A", "M4B", "M4P", "M4V",
                "MACOS",
                "MAX",
                "MEF",
                "MIE",
                "MIFF", "MIF",
                "MKA", "MKV", "MKS",
                "MOBI", "AZW", "AZW3",
                "MODD",
                "MOI",
                "MOS",
                "MOV", "QT",
                "MP3",
                "MP4",
                "MPC",
                "MPEG", "MPG", "M2V",
                "MPO",
                "MQV",
                "MRW",
                "MXF",
                "NEF",
                "NMBTEMPLATE",
                "NRW",
                "NUMBERS",
                "O",
                "ODB", "ODC", "ODF", "ODG,",
                "ODI", "ODP", "ODS", "ODT",
                "OFR",
                "OGG", "OGV",
                "ONP",
                "OPUS",
                "ORF",
                "OTF",
                "PAC",
                "PAGES",
                "PCD",
                "PCX",
                "PDB", "PRC",
                "PDF",
                "PEF",
                "PFA", "PFB",
                "PFM",
                "PGF",
                "PICT", "PCT",
                "PLIST",
                "PMP",
                "PNG", "JNG", "MNG",
                "PPM", "PBM", "PGM",
                "PPT", "PPS", "POT",
                "POTX", "POTM",
                "PPAX", "PPAM",
                "PPSX", "PPSM",
                "PPTX", "PPTM",
                "PSD", "PSB", "PSDT",
                "PSP", "PSPIMAGE",
                "QTIF", "QTI", "QIF",
                "R3D",
                "RA",
                "RAF",
                "RAM", "RPM",
                "RAR",
                "RAW",
                "RAW",
                "RIFF", "RIF",
                "RM", "RV", "RMVB",
                "RSRC",
                "RTF",
                "RW2",
                "RWL",
                "RWZ",
                "SEQ",
                "SKETCH",
                "SO",
                "SR2",
                "SRF",
                "SRW",
                "SVG",
                "SWF",
                "THM",
                "THMX",
                "TIFF", "TIF",
                "TTF", "TTC",
                "TORRENT",
                "TXT",
                "VCF", "VCARD",
                "VOB",
                "VRD",
                "VSD",
                "WAV",
                "WEBM",
                "WEBP",
                "WMA", "WMV",
                "WTV",
                "WV",
                "X3F",
                "XCF",
                "XLS", "XLT",
                "XLSX", "XLSM", "XLSB",
                "XLTX", "XLTM",
                "XMP"
        };
        for (String s : ss) add(s);
    }};
    public static boolean isSupport(String name) {
        if (name==null) return false;
        int pos = name.lastIndexOf(".");
        String ext = (pos>=0 ? name.substring(pos+1) : name).toUpperCase();
        if (ext.isEmpty()) return false;
        else return EXTS.contains(ext);
    }
}
