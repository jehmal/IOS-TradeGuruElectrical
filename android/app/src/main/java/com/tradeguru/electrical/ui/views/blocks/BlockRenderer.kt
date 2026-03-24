package com.tradeguru.electrical.ui.views.blocks

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import com.tradeguru.electrical.data.DomainMappers.ContentBlock
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun RenderBlock(block: ContentBlock) {
    val colors = LocalTradeGuruColors.current

    when (block.type) {
        ContentBlockType.TEXT -> TextBlockView(content = block.content ?: "")

        ContentBlockType.HEADING -> HeadingBlockView(
            content = block.content ?: "",
            level = block.level ?: 2
        )

        ContentBlockType.CODE -> CodeBlockView(
            content = block.content ?: "",
            language = block.language
        )

        ContentBlockType.STEP_LIST -> StepListView(
            title = block.title,
            steps = block.steps ?: emptyList()
        )

        ContentBlockType.PARTS_LIST -> PartsListView(
            items = block.items
        )

        ContentBlockType.TABLE -> TableBlockView(
            headers = block.headers,
            rows = block.rows ?: emptyList()
        )

        ContentBlockType.WARNING -> WarningCardView(
            content = block.content ?: ""
        )

        ContentBlockType.CALLOUT -> CalloutView(
            content = block.content ?: "",
            style = block.style
        )

        ContentBlockType.REGULATION -> RegulationView(
            code = block.code,
            clause = block.clause,
            summary = block.summary
        )

        ContentBlockType.LINK -> LinkBlockView(
            content = block.content,
            url = block.url
        )

        ContentBlockType.DIAGRAM_REF -> DiagramRefBlockView(
            content = block.content
        )

        ContentBlockType.TOOL_CALL -> {
            Text(
                text = block.content ?: "",
                color = colors.tradeTextSecondary,
                fontStyle = FontStyle.Italic
            )
        }
    }
}
