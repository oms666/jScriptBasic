A = "13"+"14"
assert "string string concatenation failed", A = "1314"
A = "13"+14
assert "string string concatenation failed", A = "1314"
A = 13+"14"
assert "string string concatenation failed", A = "1314"
A = ""+13+14
assert "string string concatenation failed", A = "1314"
A = 13+14+""
assert "string string concatenation failed", A = "27"