package common.model

; //***************** 输入表 *********************

/**
 * create table music_info
 * (
 * id                    int auto_increment  primary key,
 * music_name            varchar(255)  null,
 * singer_name           varchar(255)  null,
 * music_duration_minute varchar(20)   null,
 * music_duration_second int           null,
 * music_url             varchar(1000) null,
 * music_url_id          varchar(50)   null,
 * music_lyric           text          null,
 * music_comment_count   int           null,
 * music_comment         text          null
 * );
 */
case class MusicInfo(id: Integer,
                           music_name: String,
                           singer_name: String,
                           music_duration_minute: String,
                           music_duration_second: Int,
                           music_url: String,
                           music_url_id: String,
                           music_lyric: String,
                           music_comment_count: Int,
                           music_comment: String
                          )

/**
 * create table singer_info
 * (
 * id          int auto_increment
 * primary key,
 * singer_name varchar(255)  null,
 * fans_count  int           null,
 * mv_count    int           null,
 * song_count  int           null,
 * singer_type varchar(150)  null,
 * singer_url  varchar(1000) null
 * );
 */
case class UserInfo(user_id: Long,
                    username: String,
                    name: String,
                    age: Int,
                    professional: String,
                    city: String,
                    sex: String
                   )

