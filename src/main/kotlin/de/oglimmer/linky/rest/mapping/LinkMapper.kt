package de.oglimmer.linky.rest.mapping

import de.oglimmer.linky.entity.Link
import de.oglimmer.linky.rest.dto.LinkDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface LinkMapper {

    fun linkToLinkDto(link: Link): LinkDto

    fun linkDtoToLink(linkDto: LinkDto): Link

}