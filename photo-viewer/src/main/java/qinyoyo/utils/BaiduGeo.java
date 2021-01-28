package qinyoyo.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.PhotoInfo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BaiduGeo {
    private static final String address = "http://api.map.baidu.com/reverse_geocoding/v3/?ak=%s&output=json&coordtype=wgs84ll&extensions_poi=1&poi_types=旅游景点&extensions_town=true&location=%.6f,%.6f";
    private static final String defaultAK = "0G9lIXB6bpnSqgLv0QpieBnGMXK6WA6o";
    public static JSONObject getGeoInfo(double longitude, double latitude, String ak) throws IOException {
        // http://lbsyun.baidu.com/index.php?title=webapi/guide/webservice-geocoding-abroad
        String url = String.format(address, ak==null?defaultAK:ak, latitude, longitude);
        return HttpUtils.doJsonGet(url,null);
    }
    static String merge(String ... args) {
        String r = "";
        for (String s:args) {
            if (s!=null && !s.isEmpty()) r += s;
        }
        return r;
    }
    public static void seekAddressInfo(ArchiveInfo archiveInfo) {
        List<PhotoInfo> list = archiveInfo.getInfos().stream().filter(p->
                p.getAltitude()!=null && p.getLongitude()!=null &&
                p.getProvince()==null && p.getCity()==null && p.getLocation()==null && p.getCountry()==null
        ).collect(Collectors.toList());
        if (list!=null && list.size()>0) {
            new Thread() {
                @Override
                public void run() {
                    System.out.println(list.size() + " geo points need seek");
                    int count = 0;
                    for (PhotoInfo p : list) {
                        try {
                            JSONObject json = getGeoInfo(p.getLongitude(),p.getLatitude(),null);
                            int status = json.getInt("status");
                            if (status==0) {
                                JSONObject result = json.getJSONObject("result");
                                JSONObject addressComponent = result.getJSONObject("addressComponent");
                                p.setCountry("<"+addressComponent.getString("country")+">");
                                p.setProvince(addressComponent.getString("province"));
                                p.setCity(addressComponent.getString("city"));
                                p.setLocation(merge(addressComponent.getString("district"),
                                        addressComponent.getString("town"),
                                        addressComponent.getString("street")));
                                if (p.getSubjectCode()==null) {
                                    JSONArray pois = result.getJSONArray("pois");
                                    if (!pois.isNull(0)) {
                                        JSONObject poi = pois.getJSONObject(0);
                                        p.setSubjectCode(poi.getString("name"));
                                    }
                                }
                                count ++;
                            }
                        } catch (Exception e) {
                        }
                    }
                    System.out.println(count + " geo points seeked");
                    archiveInfo.saveInfos();
                }
            }.start();
        }
    }
    /*
    {
	"status": 0,
	"result": {
		"location": {
			"lng": 120.39463978340436,
			"lat": 36.27295465795934
		},
		"formatted_address": "山东省青岛市城阳区",
		"business": "流亭",
		"addressComponent": {
			"country": "中国",
			"country_code": 0,
			"country_code_iso": "CHN",
			"country_code_iso2": "CN",
			"province": "山东省",
			"city": "青岛市",
			"city_level": 2,
			"district": "城阳区",
			"town": "",
			"town_code": "",
			"adcode": "370214",
			"street": "",
			"street_number": "",
			"direction": "",
			"distance": ""
		},
		"pois": [{
			"addr": "青岛市城阳区民航路99号",
			"cp": "",
			"direction": "内",
			"distance": "0",
			"name": "青岛流亭国际机场",
			"poiType": "交通设施",
			"point": {
				"x": 120.39241358068229,
				"y": 36.273122671627188
			},
			"tag": "交通设施;飞机场",
			"tel": "",
			"uid": "ed76c77eb422da9c9868b4e3",
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
			"addr": "山东省青岛市城阳区流亭镇民航路99号国际出发",
			"cp": " ",
			"direction": "东北",
			"distance": "149",
			"name": "外币兑换(国际出发)",
			"poiType": "出入口",
			"point": {
				"x": 120.39345561507386,
				"y": 36.27243888717346
			},
			"tag": "出入口;机场入口",
			"tel": "",
			"uid": "4c7f6abbd542593e128824c2",
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
			"addr": "民航路99号青岛流亭国际机场1F层",
			"cp": " ",
			"direction": "东北",
			"distance": "151",
			"name": "青岛流亭国际机场T1航站楼(国内到达)",
			"poiType": "出入口",
			"point": {
				"x": 120.39363527617586,
				"y": 36.272213382465078
			},
			"tag": "出入口;机场出口",
			"tel": "",
			"uid": "1cc75f5eee5197851f1d11bc",
			"zip": "",
			"parent_poi": {
				"name": "青岛流亭国际机场-T1航站楼",
				"tag": "交通设施;其他",
				"addr": "山东省青岛市城阳区流亭镇民航路99号",
				"point": {
					"x": 120.39295256398828,
					"y": 36.272322497728598
				},
				"direction": "东北",
				"distance": "206",
				"uid": "152b2cc0cdf14e3beee4a53f"
			}
		}, {
			"addr": "民航路青岛流亭国际机场T1航站楼2层",
			"cp": " ",
			"direction": "东北",
			"distance": "158",
			"name": "青岛流亭国际机场-国内出发",
			"poiType": "出入口",
			"point": {
				"x": 120.39364425923096,
				"y": 36.272133364507407
			},
			"tag": "出入口;机场入口",
			"tel": "",
			"uid": "e99897945a1640553c9a5ea9",
			"zip": "",
			"parent_poi": {
				"name": "青岛流亭国际机场",
				"tag": "交通设施;飞机场",
				"addr": "青岛市城阳区民航路99号",
				"point": {
					"x": 120.39241358068229,
					"y": 36.273122671627188
				},
				"direction": "东",
				"distance": "248",
				"uid": "ed76c77eb422da9c9868b4e3"
			}
		}, {
			"addr": "流亭街道民航路青岛流亭国际机场T2航站楼",
			"cp": " ",
			"direction": "东南",
			"distance": "227",
			"name": "青岛流亭国际机场T2航站楼-东航上航国内出发",
			"poiType": "出入口",
			"point": {
				"x": 120.39298849620868,
				"y": 36.27393011144779
			},
			"tag": "出入口;机场入口",
			"tel": "",
			"uid": "b424239ede3aa0deabf0cdee",
			"zip": "",
			"parent_poi": {
				"name": "青岛流亭国际机场-T2航站楼",
				"tag": "交通设施;其他",
				"addr": "山东省青岛市城阳区流亭镇民航路99号",
				"point": {
					"x": 120.39226985180069,
					"y": 36.27465752749631
				},
				"direction": "东南",
				"distance": "352",
				"uid": "48bd0ed9fb01940d882fb51e"
			}
		}, {
			"addr": "山东省青岛市城阳区长城南路6号",
			"cp": " ",
			"direction": "西",
			"distance": "469",
			"name": "首创空港国际中心-16号楼",
			"poiType": "房地产",
			"point": {
				"x": 120.39885443118883,
				"y": 36.27277350585245
			},
			"tag": "房地产;写字楼",
			"tel": "",
			"uid": "07156f18bdd2c89e2eb96d5a",
			"zip": "",
			"parent_poi": {
				"name": "首创空港国际中心",
				"tag": "房地产;写字楼",
				"addr": "长城南路6号",
				"point": {
					"x": 120.39986053336,
					"y": 36.27315904297146
				},
				"direction": "西",
				"distance": "581",
				"uid": "fb7e729f5a2e3715dc1e522d"
			}
		}, {
			"addr": "民航路115",
			"cp": " ",
			"direction": "南",
			"distance": "447",
			"name": "青岛市公安局机场分局机场派出所",
			"poiType": "政府机构",
			"point": {
				"x": 120.39358137784525,
				"y": 36.276097791100948
			},
			"tag": "政府机构;公检法机构",
			"tel": "",
			"uid": "b4b3c231289991a77dc876ce",
			"zip": "",
			"parent_poi": {
				"name": "青岛市公安局机场分局",
				"tag": "政府机构;公检法机构",
				"addr": "新郑路2",
				"point": {
					"x": 120.39421917475734,
					"y": 36.276614242732858
				},
				"direction": "南",
				"distance": "505",
				"uid": "01a6c729d0d17936e81e97ef"
			}
		}, {
			"addr": "山东省青岛市城阳区长城南路6号24号楼",
			"cp": " ",
			"direction": "西",
			"distance": "489",
			"name": "Alphawolf中关村智能硬件企业加速器(青岛基地)",
			"poiType": "公司企业",
			"point": {
				"x": 120.39897121090512,
				"y": 36.273566400859568
			},
			"tag": "公司企业;园区",
			"tel": "",
			"uid": "24d503f6dcbbb5fceddfca9d",
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
			"addr": "山东省青岛市城阳区流亭国际机场民航路77号",
			"cp": " ",
			"direction": "西南",
			"distance": "661",
			"name": "丹顶鹤大酒店(民航路店)",
			"poiType": "酒店",
			"point": {
				"x": 120.39915985506222,
				"y": 36.27607596912511
			},
			"tag": "酒店;三星级",
			"tel": "",
			"uid": "4642844aa97dfcc0c9fa5f44",
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
			"addr": "民航路99号青岛流亭国际机场1F层",
			"cp": " ",
			"direction": "北",
			"distance": "318",
			"name": "微时光(青岛流亭国际机场)",
			"poiType": "购物",
			"point": {
				"x": 120.39415629337164,
				"y": 36.270671203622658
			},
			"tag": "购物",
			"tel": "",
			"uid": "586b218de44e043da21de96c",
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
			"addr": "山东省青岛市城阳区迎宾路100号",
			"cp": " ",
			"direction": "西北",
			"distance": "523",
			"name": "上岸寄宿考研(公)备考基地",
			"poiType": "教育培训",
			"point": {
				"x": 120.39745307459326,
				"y": 36.26990010267832
			},
			"tag": "教育培训;培训机构",
			"tel": "",
			"uid": "6bdec950416238aea10464d7",
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
			"addr": "长城南路6号2号楼108",
			"cp": " ",
			"direction": "西",
			"distance": "627",
			"name": "菜小姐饭先生",
			"poiType": "美食",
			"point": {
				"x": 120.3999054486355,
				"y": 36.27132590594139
			},
			"tag": "美食;小吃快餐店",
			"tel": "",
			"uid": "44984ab902f9366772210bf0",
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
			"addr": "山东省青岛市城阳区流亭机场28号流亭机场东侧",
			"cp": " ",
			"direction": "北",
			"distance": "956",
			"name": "中国航油(流亭机场加油站)",
			"poiType": "交通设施",
			"point": {
				"x": 120.39754290514425,
				"y": 36.2664082286632
			},
			"tag": "交通设施;加油加气站",
			"tel": "",
			"uid": "09dfdf872cc300920accddaf",
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
			"addr": "山东省青岛市城阳区长城南路6号",
			"cp": " ",
			"direction": "西",
			"distance": "484",
			"name": "首创空港国际中心-25号楼",
			"poiType": "房地产",
			"point": {
				"x": 120.39880951591333,
				"y": 36.273966482412607
			},
			"tag": "房地产;写字楼",
			"tel": "",
			"uid": "67ac0c00ee707eb9abf0cd6e",
			"zip": "",
			"parent_poi": {
				"name": "首创空港国际中心",
				"tag": "房地产;写字楼",
				"addr": "长城南路6号",
				"point": {
					"x": 120.39986053336,
					"y": 36.27315904297146
				},
				"direction": "西",
				"distance": "581",
				"uid": "fb7e729f5a2e3715dc1e522d"
			}
		}, {
			"addr": "山东省青岛市城阳区长城南路6号",
			"cp": " ",
			"direction": "西",
			"distance": "500",
			"name": "首创空港国际中心-9号楼",
			"poiType": "房地产",
			"point": {
				"x": 120.39902510923572,
				"y": 36.272147913233137
			},
			"tag": "房地产;写字楼",
			"tel": "",
			"uid": "4ec94dd6268224db744ff116",
			"zip": "",
			"parent_poi": {
				"name": "首创空港国际中心",
				"tag": "房地产;写字楼",
				"addr": "长城南路6号",
				"point": {
					"x": 120.39986053336,
					"y": 36.27315904297146
				},
				"direction": "西",
				"distance": "581",
				"uid": "fb7e729f5a2e3715dc1e522d"
			}
		}, {
			"addr": "山东省青岛市城阳区长城南路6号",
			"cp": " ",
			"direction": "西",
			"distance": "521",
			"name": "首创空港国际中心-15号楼",
			"poiType": "房地产",
			"point": {
				"x": 120.39932155005401,
				"y": 36.272846248852079
			},
			"tag": "房地产;写字楼",
			"tel": "",
			"uid": "1e170d21de219d61d618b700",
			"zip": "",
			"parent_poi": {
				"name": "首创空港国际中心",
				"tag": "房地产;写字楼",
				"addr": "长城南路6号",
				"point": {
					"x": 120.39986053336,
					"y": 36.27315904297146
				},
				"direction": "西",
				"distance": "581",
				"uid": "fb7e729f5a2e3715dc1e522d"
			}
		}, {
			"addr": "山东省青岛市城阳区长城南路6号",
			"cp": " ",
			"direction": "西",
			"distance": "525",
			"name": "首创空港国际中心3号楼北楼",
			"poiType": "房地产",
			"point": {
				"x": 120.39906104145612,
				"y": 36.27161688297207
			},
			"tag": "房地产;写字楼",
			"tel": "",
			"uid": "b6738f1c0a6c8ffa077ce8cd",
			"zip": "",
			"parent_poi": {
				"name": "首创空港国际中心-3号楼",
				"tag": "房地产;写字楼",
				"addr": "山东省青岛市城阳区长城南路6号",
				"point": {
					"x": 120.39907002451122,
					"y": 36.271515041135767
				},
				"direction": "西",
				"distance": "531",
				"uid": "10a197c127899d439e82ea12"
			}
		}, {
			"addr": "迎宾路北150米",
			"cp": " ",
			"direction": "西",
			"distance": "527",
			"name": "首创空港国际中心商务政务服务中心",
			"poiType": "房地产",
			"point": {
				"x": 120.39912392284183,
				"y": 36.27171145027152
			},
			"tag": "房地产;写字楼",
			"tel": "",
			"uid": "dd6a5e5941faae32deee8b33",
			"zip": "",
			"parent_poi": {
				"name": "首创空港国际中心",
				"tag": "房地产;写字楼",
				"addr": "长城南路6号",
				"point": {
					"x": 120.39986053336,
					"y": 36.27315904297146
				},
				"direction": "西",
				"distance": "581",
				"uid": "fb7e729f5a2e3715dc1e522d"
			}
		}, {
			"addr": "新郑路2",
			"cp": " ",
			"direction": "南",
			"distance": "505",
			"name": "青岛市公安局机场分局",
			"poiType": "政府机构",
			"point": {
				"x": 120.39421917475734,
				"y": 36.276614242732858
			},
			"tag": "政府机构;公检法机构",
			"tel": "",
			"uid": "01a6c729d0d17936e81e97ef",
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
			"addr": "山东省青岛市城阳区长城南路6号",
			"cp": " ",
			"direction": "西",
			"distance": "531",
			"name": "首创空港国际中心3号楼南楼",
			"poiType": "房地产",
			"point": {
				"x": 120.39907002451122,
				"y": 36.27150776671375
			},
			"tag": "房地产;写字楼",
			"tel": "",
			"uid": "78826be1db3210392e473361",
			"zip": "",
			"parent_poi": {
				"name": "首创空港国际中心-3号楼",
				"tag": "房地产;写字楼",
				"addr": "山东省青岛市城阳区长城南路6号",
				"point": {
					"x": 120.39907002451122,
					"y": 36.271515041135767
				},
				"direction": "西",
				"distance": "531",
				"uid": "10a197c127899d439e82ea12"
			}
		}],
		"roads": [],
		"poiRegions": [{
			"direction_desc": "内",
			"name": "青岛流亭国际机场",
			"tag": "交通设施;飞机场",
			"uid": "ed76c77eb422da9c9868b4e3",
			"distance": "0"
		}],
		"sematic_description": "青岛流亭国际机场内,外币兑换(国际出发)东北149米",
		"cityCode": 236
	}
}
     */
}
