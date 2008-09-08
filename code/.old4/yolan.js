load("stdmob.js");
yolan = {};
yolan.parse = (function (list, nextc) {
	var c, elem, result, i, t, is_ws, is_end, quoted, more;
	c = nextc();
	is_ws = (function (c) {
		return ((c === " ") || ((c === "\n") || (c === "\t")));
	});
	is_end = (function (c) {
		return ((c === undefined) || ((c === "]") || (c === "}")));
	});
	result = [];
	while (true) {
		while (is_ws(c)) {
			c = nextc();
		};
		if ((c === "[")) {
			elem = yolan.parse(true, nextc);
		} else if ((c === "{")) {
			t = yolan.parse(true, nextc);
			elem = {};
			i = 0;
			while ((i < t.length)) {
				elem[t[i]] = t[(i + 1)];
				i = (i + 2);
			};
		} else {
			elem = [];
			quoted = false;
			while (((quoted || (!is_ws(c))) && (!is_end(c)))) {
				if ((c === "\"")) {
					quoted = (!quoted);
					c = nextc();
				} else {
					if ((c === "\\")) {
						c = nextc();
						if ((c === "n")) {
							c = "\n";
						};
					};
					arrpush(elem, c);
					c = nextc();
				};
			};
			elem = arrjoin(elem, "");
		};
		if ((!list)) {
			return elem;
		} else {
			arrpush(result, elem);
			if (is_end(c)) {
				return result;
			};
		};
	};
});
yolan.print = (function (obj, result) {
	var i;
	if (is_arr(obj)) {
		arrpush(result, "[");
		for (i in obj) {
			arrpush(result, " ");
			yolan.print(obj[i], result);
			arrpush(result, "]");
		};
	} else if (is_obj(obj)) {
		arrpush(result, "{");
		for (i in obj) {
			arrpush(result, "\t");
			yolan.print(i, result);
			arrpush(result, " ");
			yolan.print(obj[i], result);
		};
		arrpush(result, "}");
	} else if (is_str(obj)) {
		arrpush(result, "\"");
		for (i in obj) {
			if (((obj[i] === "\\") || (obj[i] === "\""))) {
				arrpush(result, "\\");
			};
			arrpush(result, obj[i]);
		};
		arrpush(result, "\"");
	} else {
		arrpush(result, strcat("", obj));
	};
	return result;
});

