function addBook(id) {
        var id = id;
        $.ajax({
            url     : '/controller',
            method  : 'POST',
            data    : {id : id, command : 'booking.addBook'},
            dataType: "xml",
            success : function(xml) {
                        var output = $(xml).find('output').text();
                        var bookedBooksNumID = "bookedBooksNum";
                        $('#' + id).prop("disabled", true);
                        if (!$("#" + bookedBooksNumID).length) {
                            var msg = $("#bookedBooksNumParent").html();

                            $("#bookedBooksNumParent")
                                .html(`${msg}<span class="position-absolute top-0 start-99 translate-middle badge rounded-pill bg-success" id="${bookedBooksNumID}"></span>`);
                        }
                        $("#" + bookedBooksNumID).text(output);
                        showAndHide('#addedBookAlert');
                      },
            error   : function(xhr, status, error) {
                        var error = $(xhr.responseXML).find('error').text();
                        var errorID = "#addedBookError";
                        $(errorID).text(error);
                        showAndHide(errorID);
                      },
        });
}

function showAndHide(id) {
    $(id).show("slow", "swing", function(){ $(id).delay(700).hide("slow") } );
}

function findAuthor() {
    var query = $("#authorQuery").val();
    $('#searchResults').empty();
    $.ajax({
                url     : '/controller',
                method  : 'GET',
                data    : {query: query, command: 'author.findAll', searchBy: 'name'},
                dataType: "xml",
                success : function(xml) {
                                $(xml).find('author').each(
                                    function() {
                                        var authorID = $(this).find('id').text();
                                        var authorName = $(this).find('name').text();
                                        var li = document.createElement('li');
                                        $(li).html(`<a class="dropdown-item" onclick="chooseAuthor(${authorID},'${authorName}');">${authorName}</a></li>`);
                                        $('#searchResults').append(li);
                                        $('#searchResults').addClass("show");
                                    }
                                );
                        },
                error   : function(xhr, exception) {
                            var error = $(xhr.responseXML).find('error').text();
                            var li = document.createElement('li');
                            $(li).html(`<a class="dropdown-item" onclick='this.classList.remove("show");'>${error}</a></li>`);
                            $('#searchResults').append(li);
                            $('#searchResults').addClass("show");
                          },
    });
}

function chooseAuthor(authorID, authorName) {
    if (document.getElementById("selectedAuthors" + authorID) == null) {
        var newSelected = document.createElement("div");
        newSelected.classList.add("form-check");
        newSelected.innerHTML = '<input type="checkbox" name="authorIDs" class="form-check-input" id="selectedAuthors'+ authorID + '"' +
                                    "value='" + authorID + "' checked/>" +
                                        '<label class="form-check-label" for="selectedAuthors'+ authorID + '">' +
                                          authorName +
                                        '</label>';

        document.getElementById('selectedAuthors').appendChild(newSelected);
        document.getElementById('searchResults').classList.remove("show");
    }
}

function check(el) {
        if (el.reportValidity()) {
            el.classList.remove("is-invalid");
            el.classList.add("is-valid");
        } else {
            el.classList.remove("is-valid");
            el.classList.add("is-invalid");
        }
}

function checkPass() {
      if (document.getElementById('password').value != null && document.getElementById('password').value ==
        document.getElementById('confirmPass').value) {
            document.getElementById('password').classList.remove("is-invalid");
            document.getElementById('confirmPass').classList.remove("is-invalid");
            document.getElementById('password').classList.add("is-valid");
            document.getElementById('confirmPass').classList.add("is-valid");
            document.getElementById('message').hidden = true;
      } else {
            document.getElementById('password').classList.add("is-invalid");
            document.getElementById('confirmPass').classList.add("is-invalid");
            document.getElementById('message').hidden = false;
      }
}

function makeValid(el) {
    el.classList.add("is-valid");
}

function toggleBooking(className) {
    var bookings = document.getElementsByClassName("accordion-item");
    for (i=0; i < bookings.length; i++) {
        var booking = bookings[i];
        if (className == 'ALL') {
            booking.hidden = false;
        } else if (booking.classList.contains('HEADER')) {
            continue;
        }
        else if (booking.classList.contains(className)) {
            booking.hidden = false;
        } else {
            booking.hidden = true;
        }
    }
}