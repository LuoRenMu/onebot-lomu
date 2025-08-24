package cn.luorenmu

import cn.luorenmu.entiy.Request
import cn.luorenmu.request.RequestController
import com.alibaba.fastjson2.JSON

fun main() {

    println("BV1hQe4zwEsR".contains(Regex("((https://bili2233.cn/([a-zA-Z0-9]+))|(https://b23.tv/([a-zA-Z0-9]+))|(BV1[0-9a-zA-Z]{9}))")))

}

fun groupList() {
    val requestController = RequestController(Request.RequestDetailed().apply {
        url = "http://192.168.1.108:3000/get_group_list"
        method = "POST"
    })
    val resp = requestController.request()
    val parseObject = JSON.parseObject(resp.body())
    val array = parseObject.getJSONArray("data")
    val groupList = mutableListOf<Long>()
    for (i in 0 until array.size) {
        groupList.add(array.getJSONObject(i).getString("group_id").toLong())
    }
    println(groupList)
}