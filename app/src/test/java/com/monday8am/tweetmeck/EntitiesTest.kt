package com.monday8am.tweetmeck

import com.monday8am.tweetmeck.data.models.entities.EntityLinkType
import com.monday8am.tweetmeck.data.models.entities.MediaEntity
import com.monday8am.tweetmeck.data.models.entities.UrlEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class EntitiesTest {

    @Test
    fun `get url entity from string`() {
        val content = "1\n2\nurl\ndisplayUrl\nexpandedUrl\nMentionedUser"
        val entity = UrlEntity.fromEntityString(content)

        assertEquals(entity.start, 1)
        assertEquals(entity.end, 2)
        assertEquals(entity.appUrl, "url")
        assertEquals(entity.displayUrl, "displayUrl")
        assertEquals(entity.webUrl, "expandedUrl")
        assertEquals(entity.entityType, EntityLinkType.MentionedUser)
    }

    @Test
    fun `get string from url entity`() {
        val entity = UrlEntity(
            start = 3,
            end = 4,
            appUrl = "_url",
            displayUrl = "_displayUrl",
            webUrl = "_expandedUrl",
            entityType = EntityLinkType.Hashtag
        )
        val content = "3\n4\n_url\n_displayUrl\n_expandedUrl\nHashtag"
        assertEquals(entity.toEntityString(), content)
    }

    @Test
    fun `get media entity from string`() {
        val content = "1\n2\nurl\ndisplayUrl\nexpandedUrl\nhttp://1\nvideo"
        val entity = MediaEntity.fromEntityString(content)

        assertEquals(entity.start, 1)
        assertEquals(entity.end, 2)
        assertEquals(entity.url, "url")
        assertEquals(entity.displayUrl, "displayUrl")
        assertEquals(entity.expandedUrl, "expandedUrl")
        assertEquals(entity.mediaUrlHttps, "http://1")
        assertEquals(entity.type, "video")
    }

    @Test
    fun `get string from media entity`() {
        val entity = MediaEntity(
            start = 3,
            end = 4,
            url = "_url",
            displayUrl = "_displayUrl",
            expandedUrl = "_expandedUrl",
            mediaUrlHttps = "http://2",
            type = "Image"
        )
        val content = "3\n4\n_url\n_displayUrl\n_expandedUrl\nhttp://2\nImage"
        assertEquals(entity.toEntityString(), content)
    }
}
