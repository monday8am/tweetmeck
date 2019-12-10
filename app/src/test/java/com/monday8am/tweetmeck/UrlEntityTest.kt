package com.monday8am.tweetmeck

import com.monday8am.tweetmeck.data.models.entities.EntityLinkType
import com.monday8am.tweetmeck.data.models.entities.UrlEntity
import org.junit.Assert.assertEquals
import org.junit.Test


class UrlEntityTest {

    @Test
    fun `get entity from string`() {
        val content = "1\n2\nurl\ndisplayUrl\nexpandedUrl\nMentionedUser"
        val entity = UrlEntity.fromEntityString(content)

        assertEquals(entity.start, 1)
        assertEquals(entity.end, 2)
        assertEquals(entity.url, "url")
        assertEquals(entity.displayUrl, "displayUrl")
        assertEquals(entity.expandedUrl, "expandedUrl")
        assertEquals(entity.entityType, EntityLinkType.MentionedUser)
    }

    @Test
    fun `get string from entity`() {
        val entity = UrlEntity(
            start = 3,
            end = 4,
            url = "_url",
            displayUrl = "_displayUrl",
            expandedUrl = "_expandedUrl",
            entityType = EntityLinkType.Hashtag
        )
        val content = "3\n4\n_url\n_displayUrl\n_expandedUrl\nHashtag"
        assertEquals(entity.toEntityString(), content)
    }
}
