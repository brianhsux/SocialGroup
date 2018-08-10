package com.brianhsu.socialgroup.model

class SectionImage(val image: Int, val title: String, val section: Boolean) {
    override fun toString(): String {
        return "#$title"
    }
}