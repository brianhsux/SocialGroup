package com.brianhsu.socialgroup.Utilities

// Debug Tag
const val TAG = "BBB>>>SocialGroup"

const val BASE_URL = "https://webdevbootcamp-brianhsux.c9users.io/v1/"
const val BASE_HEROKU_URL = "https://socialgroupapi.herokuapp.com/v1/"
//const val BASE_URL = "https://10.0.2.2:3005/v1/"
const val SOCKET_URL = "https://chattychatbh.herokuapp.com/"
const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_CREATE_USER = "${BASE_URL}user/add"
const val URL_GET_USER = "${BASE_URL}user/byEmail/"
const val URL_GET_CHANNELS = "${BASE_URL}channel/"
const val URL_GET_MESSAGES = "${BASE_URL}message/byChannel/"

const val URL_CREATE_POST = "${BASE_URL}post/add"
const val URL_READ_POST = "${BASE_URL}post"

// Broadcast Constants
const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"
const val BROADCAST_SEND_POST_ACTION = "BROADCAST_SEND_POST_ACTION"
const val SEND_POST_ACTION_RESULT_CODE = 1001

// CreatePostActivity
const val CHOOSE_IMAGE_REQUEST_CODE = 1000