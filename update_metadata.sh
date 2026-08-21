#!/bin/bash

# DTOs and Models - renaming coverDir -> coverUrl, adding coverPublicId
find src/main/java -type f -name "*.java" | while read -r file; do
    # Replace the DTO fields and model fields
    sed -i 's/String coverDir/String coverUrl,\n        String coverPublicId/g' "$file"
    sed -i 's/private String coverDir/private String coverUrl;\n    @Column(name = "cover_public_id")\n    private String coverPublicId/g' "$file"
    sed -i 's/@Column(name = "cover_dir")/@Column(name = "cover_url")/g' "$file"

    # For usages like getCoverDir(), setCoverDir()
    sed -i 's/getCoverDir()/getCoverUrl()/g' "$file"
    sed -i 's/setCoverDir(dto.coverDir())/setCoverUrl(dto.coverUrl());\n        existingCourse.setCoverPublicId(dto.coverPublicId());/g' "$file"
    sed -i 's/setCoverDir(dto.coverDir())/setCoverUrl(dto.coverUrl());\n        course.setCoverPublicId(dto.coverPublicId());/g' "$file"
    
    # In constructors or map methods (a bit tricky, let's just do generic replacements where possible)
    # Actually, let's just do it file by file to be safe.
done
