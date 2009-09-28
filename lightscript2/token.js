function Token(type, val, subtype, start, end, newline, comment) {
    return {"type": type,
            "val": val,
            "subtype": subtype,
            "start": start,
            "end": end,
            "newline": newline,
            "comment": comment};
}

