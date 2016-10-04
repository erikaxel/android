#!/usr/bin/ruby

sizes = [
  # ['mipmap-hdpi', '36.png', '36x36'],
  ['mipmap-mdpi', '48.png', '48x48'],
  ['mipmap-hdpi', '72.png', '72x72'],
  ['mipmap-xhdpi', '96.png', '96x96'],
  ['mipmap-xxhdpi', '144.png', '144x144'],
  ['mipmap-xxxhdpi', '192.png', '192x192'],
  [nil, '512.png', '512x512']
]

src_file = ARGV[0] 
dst_path = ARGV[1]

sizes.each do |size|
  puts "Converting #{size[1]}"
  `convert "logo.png" -resize #{size[2]} "output/#{size[1]}"`
  `cp output/#{size[1]} ../app/src/main/res/#{size[0]}/ic_launcher.png` if size[0]
end

