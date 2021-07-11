package com.goelapplications.igsaver.models

import com.goelapplications.igsaver.constants.MediaType
import com.goelapplications.igsaver.constants.PostType

data class MediaModel(var mediaType: MediaType,
                      var downloadUrl: String? = null,
                      var thumbnailUrl: String? = null)
