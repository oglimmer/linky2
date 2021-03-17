package de.oglimmer.linky.rest.mapping

import de.oglimmer.linky.entity.Tags
import de.oglimmer.linky.rest.dto.TagsDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface TagsMapper {

    fun tagsToTagsDto(tags: Tags): TagsDto

}