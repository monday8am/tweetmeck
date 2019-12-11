package com.monday8am.tweetmeck.util

import com.monday8am.tweetmeck.data.models.entities.*

object TweetTextUtils {

    /**
    fun getContentForEntity(entity: TweetEntity): String {
        return when(entity.type) {
            EntityLinkType.Hashtag -> "[`${entity.content}`](${entity.value}_${entity.type})"
            EntityLinkType.MentionedUser -> "[*${entity.content}*](${entity.value}_${entity.type})"
            EntityLinkType.Url -> "[${entity.content}](${entity.value}_${entity.type})"
        }
    }

     * Shifts indices by 1 since the Twitter REST Api does not count them correctly for our language
     * runtime.
     *
     * @param entities The entities that need to be adjusted
     * @param indices The indices in the string where there are supplementary chars

    fun adjustEntitiesWithOffsets(entities: List<UrlEntity>, indices: List<Int>) {
        for (entity in entities) {
            // find all indices <= start and update offsets by that much
            val start = entity.start
            var offset = 0
            for (index in indices) {
                if (index - offset <= start) {
                    offset += 1
                } else {
                    break
                }
            }
            entity.start = entity.start + offset
            entity.end = entity.end + offset
        }
    }
     */
}
