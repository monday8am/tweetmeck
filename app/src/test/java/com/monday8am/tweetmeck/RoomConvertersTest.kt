package com.monday8am.tweetmeck

import com.monday8am.tweetmeck.data.local.RoomConverters
import com.monday8am.tweetmeck.data.models.entities.EntityLinkType
import com.monday8am.tweetmeck.data.models.entities.UrlEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomConvertersTest {

    private val converter = RoomConverters
    private val sp = converter.separator

    private val entityA = UrlEntity(
        start = 1,
        end = 3,
        displayUrl = "displayUrl",
        appUrl = "url",
        webUrl = "expandedUrl",
        entityType = EntityLinkType.Hashtag
    )

    private val entityB = UrlEntity(
        start = 2,
        end = 4,
        displayUrl = "displayUrl",
        appUrl = "url",
        webUrl = "expandedUrl",
        entityType = EntityLinkType.Symbol
    )

    @Test
    fun `test  UrlEntity list to String`() {
        val strList = converter.fromUrlEntities(listOf(entityA, entityB, entityB))
        val result = "${entityA.toEntityString()}${sp}${entityB.toEntityString()}${sp}${entityB.toEntityString()}"
        assertEquals(strList, result)
    }

    @Test
    fun `test string to UrlEntity list`() {
        val strList = "${entityA.toEntityString()}${sp}${entityA.toEntityString()}${sp}${entityB.toEntityString()}"
        val entityList = converter.toUrlEntities(strList)
        assertEquals(listOf(entityA, entityA, entityB), entityList)
    }

    @Test
    fun `test single unity entity list to string`() {
        val strList = converter.fromUrlEntities(listOf(entityA))
        val result = entityA.toEntityString()
        assertEquals(strList, result)
    }

    @Test
    fun `test single unity string list to entity`() {
        val strList = entityA.toEntityString()
        val result = converter.toUrlEntities(strList)
        assertEquals(listOf(entityA), result)
    }

    @Test
    fun `test empty entity list to string`() {
        val strList = converter.fromUrlEntities(listOf())
        assertTrue(strList.isEmpty())
    }

    @Test
    fun `test entity list from empty string`() {
        val result = converter.toUrlEntities("")
        val emptyList: List<UrlEntity> = listOf()
        assertEquals(result, emptyList)
    }
}
