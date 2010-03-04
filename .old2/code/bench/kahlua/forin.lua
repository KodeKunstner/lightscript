local arr = {}
local i = 0
while i < 100 do
    arr[i] = i
    i = i + 1
end

for x in pairs(arr) do
    for y in pairs(arr) do
        for z in pairs(arr) do
                    i = i + 1
        end
    end
end

print(i)
