package qinyoyo.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import tang.qinyoyo.ArchiveUtils;
import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.PhotoInfo;
import tang.qinyoyo.exiftool.Key;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BaiduGeo {
    private static final String address = "http://api.map.baidu.com/reverse_geocoding/v3/?ak=%s&output=json&coordtype=wgs84ll&extensions_poi=1&poi_types=旅游景点&extensions_town=true&location=%.6f,%.6f";
    private static final String defaultAK = "0G9lIXB6bpnSqgLv0QpieBnGMXK6WA6o";
    public static JSONObject getGeoInfo(double longitude, double latitude, String ak) throws IOException {
        // http://lbsyun.baidu.com/index.php?title=webapi/guide/webservice-geocoding-abroad
        String url = String.format(address, ak==null?defaultAK:ak, latitude, longitude);
        //System.out.println(url);
        return HttpUtils.doJsonGet(url,null);
    }

    static String trunc(String s,Key key) {
        int maxLength = Key.getMaxLength(key);
        if (maxLength==0) return s;
        try {
            int utf8size = s.getBytes("utf-8").length;
            while (utf8size > maxLength) {
                s=s.substring(0,s.length()-1);
                utf8size = s.getBytes("utf-8").length;
            }
            return s;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
    public static void seekAddressInfo(ArchiveInfo archiveInfo) {
        List<PhotoInfo> list = archiveInfo.getInfos().stream().filter(p->
                p.getLatitude()!=null && p.getLongitude()!=null
                && p.getProvince()==null && p.getCity()==null && p.getLocation()==null && p.getCountry()==null
        ).collect(Collectors.toList());
        //ArchiveUtils.writeAddress(archiveInfo.getInfos(),archiveInfo.getPath());
        if (list!=null && list.size()>0) {
            list.sort((a,b)->{
                int r = a.getSubFolder().compareTo(b.getSubFolder());
                return r==0 ? a.compareTo(b) : r;
            });
            List<PhotoInfo> changedList = new ArrayList<>();
            new Thread() {
                @Override
                public void run() {
                    System.out.println(list.size() + " geo points need seek");
                    int count = 0;
                    double delta = 0.0001;
                    PhotoInfo p0 = null;
                    for (PhotoInfo p : list) {
                        try {
                            if (p0!=null && Math.abs(p.getLatitude()-p0.getLatitude())<delta && Math.abs(p.getLongitude()-p0.getLongitude())<delta) {
                                p.setCountry(p0.getCountry());
                                p.setProvince(p0.getProvince());
                                p.setCity(p0.getCity());
                                p.setLocation(p0.getLocation());
                                if (p.getSubjectCode() == null) p.setSubjectCode(p0.getSubjectCode());
                                else if (p0.getSubjectCode()==null) p0.setSubjectCode(p.getSubjectCode());
                                count ++;
                                changedList.add(p);
                            } else {
                                JSONObject json = getGeoInfo(p.getLongitude(), p.getLatitude(), null);
                                int status = json.getInt("status");
                                if (status == 0) {
                                    JSONObject result = json.getJSONObject("result");
                                    JSONObject addressComponent = result.getJSONObject("addressComponent");
                                    p.setCountry(trunc(addressComponent.getString("country"),Key.COUNTRY));
                                    p.setProvince(trunc(addressComponent.getString("province"),Key.STATE));
                                    p.setCity(trunc(addressComponent.getString("city"),Key.CITY));
                                    boolean cc = ArchiveUtils.hasChinese(p.getCountry()) || ArchiveUtils.hasChinese(p.getProvince()) ||ArchiveUtils.hasChinese(p.getCity());
                                    String loc = cc ? ArchiveUtils.join(null, addressComponent.getString("district"),
                                            addressComponent.getString("town"),
                                            addressComponent.getString("street"))
                                            : ArchiveUtils.join(",", addressComponent.getString("street"),
                                            addressComponent.getString("town"),
                                            addressComponent.getString("district"));
                                    String newLoc = trunc(loc,Key.LOCATION);
                                    if (newLoc.length()<loc.length()) {
                                        loc = cc ? ArchiveUtils.join(null,addressComponent.getString("district"),
                                                addressComponent.getString("street")) :
                                                ArchiveUtils.join(",",addressComponent.getString("street"),
                                                        addressComponent.getString("district"));
                                        if (loc==null || loc.isEmpty()) loc = newLoc;
                                        else newLoc = trunc(loc,Key.LOCATION);
                                        if (newLoc.length()<loc.length()) {
                                            loc = addressComponent.getString("street");
                                            if (loc==null || loc.isEmpty()) loc = addressComponent.getString("district");
                                            newLoc = trunc(loc,Key.LOCATION);
                                        }
                                    }
                                    p.setLocation(newLoc);
                                    if (p.getSubjectCode() == null) {
                                        JSONArray pois = result.getJSONArray("pois");
                                        if (!pois.isNull(0)) {
                                            JSONObject poi = pois.getJSONObject(0);
                                            if ("内".equals(poi.get("direction")) || "0".equals(poi.get("distance"))) p.setSubjectCode(trunc(poi.getString("name"),Key.SUBJECT_CODE));
                                        }
                                    }
                                    count++;
                                    p0 = p;
                                    changedList.add(p);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(count + " geo points seeked");
                    archiveInfo.saveInfos();
                    ArchiveUtils.writeAddress(changedList,archiveInfo.getPath());
                }
            }.start();
        }
    }

/*
http://api.map.baidu.com/reverse_geocoding/v3/?ak=0G9lIXB6bpnSqgLv0QpieBnGMXK6WA6o&output=json&coordtype=wgs84ll&extensions_poi=1&poi_types=旅游景点&extensions_town=true&location=25.6083333,100.2405556
{
	"status": 0,
	"result": {
		"location": {
			"lng": 100.24821060447664,
			"lat": 25.6111297965444
		},
		"formatted_address": "云南省大理白族自治州大理市滨海大道",
		"business": "开发区,云鹤路,文华路",
		"addressComponent": {
			"country": "中国",
			"country_code": 0,
			"country_code_iso": "CHN",
			"country_code_iso2": "CN",
			"province": "云南省",
			"city": "大理白族自治州",
			"city_level": 2,
			"district": "大理市",
			"town": "洱海管理处",
			"town_code": "532901400",
			"adcode": "532901",
			"street": "滨海大道",
			"street_number": "",
			"direction": "",
			"distance": ""
		},
		"pois": [{
			"addr": "大理白族自治州下关滨海大道1102号",
			"cp": "",
			"direction": "内",
			"distance": "0",
			"name": "海心亭",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24900844194524,
				"y": 25.610473367864285
			},
			"tag": "旅游景点;风景区",
			"tel": "",
			"uid": "3c06011627a125b4a20464c5",
			"zip": "",
			"parent_poi": {
				"name": "团山",
				"tag": "旅游景点;公园",
				"addr": "大理白族自治州河洱路1号",
				"point": {
					"x": 100.24971810329788,
					"y": 25.609308571272736
				},
				"direction": "西北",
				"distance": "279",
				"uid": "2ac19cc08367e457d4851f18"
			}
		}, {
			"addr": "云南省大理白族自治州大理市滨海大道",
			"cp": "",
			"direction": "内",
			"distance": "0",
			"name": "洱海公园",
			"poiType": "旅游景点",
			"point": {
				"x": 100.25338318977738,
				"y": 25.608909442528167
			},
			"tag": "旅游景点;风景区",
			"tel": "",
			"uid": "3978b0b469013870b1077d85",
			"zip": "",
			"parent_poi": {
				"name": "",
				"tag": "",
				"addr": "",
				"point": {
					"x": 0.0,
					"y": 0.0
				},
				"direction": "",
				"distance": "",
				"uid": ""
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "西北",
			"distance": "212",
			"name": "永花亭",
			"poiType": "旅游景点",
			"point": {
				"x": 100.2496282727469,
				"y": 25.609968352569387
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "884c34461959170befe4a556",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州河洱路1号",
			"cp": " ",
			"direction": "西北",
			"distance": "279",
			"name": "团山",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24971810329788,
				"y": 25.609308571272736
			},
			"tag": "旅游景点;公园",
			"tel": "",
			"uid": "2ac19cc08367e457d4851f18",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "北",
			"distance": "319",
			"name": "听涛",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24793047533362,
				"y": 25.60854289453507
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "5542c57af0e475545fd4fef6",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "西北",
			"distance": "329",
			"name": "映雪亭",
			"poiType": "旅游景点",
			"point": {
				"x": 100.25071522241362,
				"y": 25.60969955322631
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "fabae165fc2f8921c7f02533",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "西北",
			"distance": "330",
			"name": "天龙台",
			"poiType": "旅游景点",
			"point": {
				"x": 100.25023912049349,
				"y": 25.609161952706807
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "3876d7b69242b4931d58007e",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "西北",
			"distance": "338",
			"name": "杜鹃池",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24981691690394,
				"y": 25.608787259990064
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "373d10bcd65c254e4c6d4612",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "北",
			"distance": "346",
			"name": "岩石园",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24782267867245,
				"y": 25.608331110732217
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "6b6d41d1b2cbe9ab89fc0697",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "北",
			"distance": "391",
			"name": "息龙池",
			"poiType": "旅游景点",
			"point": {
				"x": 100.249080306386,
				"y": 25.60803787099432
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "103f06f3e10709ebece4a59e",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "北",
			"distance": "429",
			"name": "洗尘池",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24762505146032,
				"y": 25.607671320299258
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "8296fc3b8151228aabf0cd68",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "北",
			"distance": "434",
			"name": "杜鹃园",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24804725504987,
				"y": 25.607589864434926
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "c7b59a0d44950e52306212ad",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州滨海大道洱海公园内",
			"cp": " ",
			"direction": "西北",
			"distance": "437",
			"name": "盆景园",
			"poiType": "旅游景点",
			"point": {
				"x": 100.25173030763956,
				"y": 25.609552935145236
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "df0d63cc9e26d862b75f1ed1",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州洱海公园山茶园内",
			"cp": " ",
			"direction": "西",
			"distance": "499",
			"name": "玉洱银苍",
			"poiType": "旅游景点",
			"point": {
				"x": 100.25243098593711,
				"y": 25.60974028043879
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "56bb33ea7557ada68c1a89cf",
			"zip": "",
			"parent_poi": {
				"name": "山茶园",
				"tag": "旅游景点;景点",
				"addr": "大理白族自治州下关洱河南路1号洱海公园内",
				"point": {
					"x": 100.25270047759003,
					"y": 25.609577371504714
				},
				"direction": "西",
				"distance": "534",
				"uid": "881a28e73d1b763af942b2f6"
			}
		}, {
			"addr": "大理白族自治州滨海大道洱海公园内",
			"cp": " ",
			"direction": "西北",
			"distance": "505",
			"name": "艳甲洗云",
			"poiType": "旅游景点",
			"point": {
				"x": 100.25235912149634,
				"y": 25.609455189656843
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "a53c420bf59a8d1c4e776ef4",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州滨海大道洱海公园内",
			"cp": " ",
			"direction": "东北",
			"distance": "530",
			"name": "百二山河牌坊",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24479538910482,
				"y": 25.608119326550019
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "2c264c982cc8f1f69222e085",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "东北",
			"distance": "532",
			"name": "曲径通幽",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24526250796986,
				"y": 25.607712048210375
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "b0e3a82f0a9484c7dc1e521e",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "西",
			"distance": "534",
			"name": "山茶园",
			"poiType": "旅游景点",
			"point": {
				"x": 100.25270047759003,
				"y": 25.609577371504714
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "881a28e73d1b763af942b2f6",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州下关洱河南路1号洱海公园内",
			"cp": " ",
			"direction": "东北",
			"distance": "552",
			"name": "鹤归亭",
			"poiType": "旅游景点",
			"point": {
				"x": 100.24560386406354,
				"y": 25.60729662285871
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "016858e30a0f7b7d2fe67bef",
			"zip": "",
			"parent_poi": {
				"name": "洱海公园",
				"tag": "旅游景点;风景区",
				"addr": "云南省大理白族自治州大理市滨海大道",
				"point": {
					"x": 100.25338318977738,
					"y": 25.608909442528167
				},
				"direction": "西北",
				"distance": "637",
				"uid": "3978b0b469013870b1077d85"
			}
		}, {
			"addr": "大理白族自治州洱海公园山茶园内",
			"cp": " ",
			"direction": "西",
			"distance": "591",
			"name": "满庭芳",
			"poiType": "旅游景点",
			"point": {
				"x": 100.25326641006112,
				"y": 25.609650680552826
			},
			"tag": "旅游景点;景点",
			"tel": "",
			"uid": "f1d6171f196eac289703192b",
			"zip": "",
			"parent_poi": {
				"name": "山茶园",
				"tag": "旅游景点;景点",
				"addr": "大理白族自治州下关洱河南路1号洱海公园内",
				"point": {
					"x": 100.25270047759003,
					"y": 25.609577371504714
				},
				"direction": "西",
				"distance": "534",
				"uid": "881a28e73d1b763af942b2f6"
			}
		}],
		"roads": [],
		"poiRegions": [{
			"direction_desc": "内",
			"name": "海心亭",
			"tag": "旅游景点;风景区",
			"uid": "3c06011627a125b4a20464c5",
			"distance": "0"
		}, {
			"direction_desc": "内",
			"name": "洱海公园",
			"tag": "旅游景点;风景区",
			"uid": "3978b0b469013870b1077d85",
			"distance": "0"
		}],
		"sematic_description": "洱海公园内,海心亭内0米",
		"cityCode": 111
	}
}
     */
}
